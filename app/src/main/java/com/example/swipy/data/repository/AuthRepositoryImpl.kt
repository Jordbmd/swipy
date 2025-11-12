package com.example.swipy.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Room
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.mapper.toEntity
import com.example.swipy.data.mapper.toUser
import com.example.swipy.data.remote.UserRemoteDataSource
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.SwipeRepositoryImpl
import com.example.swipy.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(context: Context) : AuthRepository {

    private val userRemoteDataSource = UserRemoteDataSource()
    private val swipeRepositoryImpl = SwipeRepositoryImpl(context)

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    ).fallbackToDestructiveMigration().build()

    private val userDao = db.userDao()

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "swipy_auth_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_IS_OFFLINE_MODE = "is_offline_mode"
    }

    override suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val localUser = userDao.getUserByEmailAndPassword(email, password)

                if (localUser != null) {
                    try {
                        val apiResult = userRemoteDataSource.login(email, password)
                        if (apiResult.isSuccess) {
                            prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, false) }
                            syncUsersFromApi()
                            swipeRepositoryImpl.syncSwipesFromApi(localUser.id)

                            swipeRepositoryImpl.syncPendingSwipes()
                        } else {
                            prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, true) }
                        }
                    } catch (_: Exception) {
                        prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, true) }
                    }

                    prefs.edit { putString(KEY_CURRENT_USER_ID, localUser.id.toString()) }

                    return@withContext Result.success(localUser.toUser())
                }

                val apiResult = userRemoteDataSource.login(email, password)

                apiResult.onSuccess { userResponse ->
                    prefs.edit {
                        putString(KEY_CURRENT_USER_ID, userResponse.id)
                        putBoolean(KEY_IS_OFFLINE_MODE, false)
                    }

                    syncUsersFromApi()

                    val insertedUser = userDao.getUserById(userResponse.id.toIntOrNull() ?: 0)

                    return@withContext if (insertedUser != null) {
                        Result.success(insertedUser.toUser())
                    } else {
                        Result.failure(Exception("Erreur de synchronisation"))
                    }
                }.onFailure { error ->
                    return@withContext Result.failure(Exception("Email ou mot de passe incorrect"))
                }

                Result.failure(Exception("Erreur inconnue"))
            } catch (_: Exception) {
                Result.failure(Exception("Erreur de connexion. Vérifiez vos identifiants."))
            }
        }

    override suspend fun register(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int,
        gender: String,
        bio: String?,
        city: String?,
        country: String?,
        photos: List<String>
    ): Result<User> = withContext(Dispatchers.IO) {
        try {

            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("Email déjà utilisé"))
            }

            try {
                val apiResult = userRemoteDataSource.register(
                    email = email,
                    password = password,
                    firstname = firstname,
                    lastname = lastname,
                    age = age,
                    gender = gender,
                    bio = bio ?: "",
                    city = city ?: "",
                    country = country ?: ""
                )

                apiResult.onSuccess { userResponse ->
                    val userEntity = UserEntity(
                        id = userResponse.id.toIntOrNull() ?: 0,
                        email = userResponse.email,
                        password = userResponse.password,
                        firstname = userResponse.firstname,
                        lastname = userResponse.lastname,
                        age = userResponse.age,
                        gender = userResponse.gender ?: "other",
                        bio = userResponse.bio ?: "",
                        city = userResponse.city ?: "",
                        country = userResponse.country ?: "",
                        latitude = userResponse.latitude ?: 0.0,
                        longitude = userResponse.longitude ?: 0.0,
                        maxDistance = userResponse.maxDistance ?: 50,
                        preferredGender = userResponse.preferredGender ?: "all",
                        photos = photos
                    )

                    userDao.insert(userEntity)

                    prefs.edit {
                        putString(KEY_CURRENT_USER_ID, userResponse.id)
                        putBoolean(KEY_IS_OFFLINE_MODE, false)
                    }

                    return@withContext Result.success(userEntity.toUser())
                }
            } catch (_: Exception) {
            }

            val localUser = UserEntity(
                id = System.currentTimeMillis().toInt(),
                email = email,
                password = password,
                firstname = firstname,
                lastname = lastname,
                age = age,
                gender = gender,
                bio = bio ?: "",
                city = city ?: "",
                country = country ?: "",
                latitude = 0.0,
                longitude = 0.0,
                maxDistance = 50,
                preferredGender = "all",
                photos = photos
            )

            userDao.insert(localUser)

            prefs.edit {
                putString(KEY_CURRENT_USER_ID, localUser.id.toString())
                putBoolean(KEY_IS_OFFLINE_MODE, true)
            }

            Result.success(localUser.toUser())

        } catch (_: Exception) {
            Result.failure(Exception("Erreur lors de l'inscription"))
        }
    }

    private suspend fun syncUsersFromApi() {
        try {
            val result = userRemoteDataSource.getUsers()
            result.onSuccess { userResponses ->
                userResponses.forEach { userResponse ->
                    val userEntity = userResponse.toEntity()
                    userDao.insert(userEntity)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun isOfflineMode(): Boolean {
        return prefs.getBoolean(KEY_IS_OFFLINE_MODE, false)
    }

    override suspend fun logout() {
        prefs.edit {
            remove(KEY_CURRENT_USER_ID)
            remove(KEY_IS_OFFLINE_MODE)
        }
    }

    override suspend fun currentUserIdOrNull(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, null)
    }

    override suspend fun currentUserOrNull(): User? = try {
        val userId = currentUserIdOrNull()?.toIntOrNull() ?: return null

        val entity =
            userDao.getUserById(userId)
         ?: return null

        entity.toUser()
    } catch (_: Exception) {
        null
    }
    
   
    override suspend fun updateUser(
        userId: Int,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String?,
        city: String?,
        country: String?,
        maxDistance: Int,
        photos: List<String>
    ): User = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserById(userId)
            ?: throw Exception("Utilisateur non trouvé")
        
        val updatedUser = currentUser.copy(
            firstname = firstname,
            lastname = lastname,
            age = age,
            bio = bio ?: "",
            city = city ?: "",
            country = country ?: "",
            maxDistance = maxDistance,
            photos = photos
        )
        
        userDao.update(updatedUser)
        
        try {
            val result = userRemoteDataSource.updateUser(
                userId = userId.toString(),
                firstname = firstname,
                lastname = lastname,
                age = age,
                bio = bio ?: "",
                city = city ?: "",
                country = country ?: ""
            )
            
            result.onSuccess {
                prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, false) }
            }.onFailure {
                prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, true) }
            }
        } catch (e: Exception) {
            prefs.edit { putBoolean(KEY_IS_OFFLINE_MODE, true) }
        }
        
        updatedUser.toUser()
    }
}