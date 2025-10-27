package com.example.swipy.repositories

import androidx.room.Room
import android.content.Context
import com.example.swipy.data.local.AppDatabase
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.models.User

class LocalAuthRepository(context: Context) : AuthRepository {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val userDao = db.userDao()

    private var currentUser: User? = null

    override fun currentUserIdOrNull(): String? = currentUser?.id?.toString()
    override fun currentUserOrNull(): User? = currentUser

    override suspend fun login(email: String, password: String): Result<User> {
        val userEntity = userDao.login(email, password)
        return if (userEntity != null) {
            val user = User(
                id = userEntity.id,
                email = userEntity.email,
                password = userEntity.password,
                firstname = userEntity.firstname,
                lastname = userEntity.lastname,
                age = userEntity.age,
                gender = userEntity.gender,
                bio = userEntity.bio,
                city = userEntity.city,
                country = userEntity.country,
                latitude = userEntity.latitude,
                longitude = userEntity.longitude,
                maxDistance = userEntity.maxDistance,
                preferredGender = userEntity.preferredGender,
                photos = userEntity.photos ?: emptyList()
            )

            currentUser = user
            Result.success(user)
        } else {
            Result.failure(Exception("Email ou mot de passe incorrect"))
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
    ): Result<User> {
        val existing = userDao.getByEmail(email)
        if (existing != null) return Result.failure(Exception("Email déjà utilisé"))

        val entity = UserEntity(
            email = email,
            password = password,
            firstname = firstname,
            lastname = lastname,
            age = age,
            gender = gender,
            bio = bio,
            city = city,
            country = country,
            latitude = null,
            longitude = null,
            maxDistance = 50,
            preferredGender = null,
            photos = photos
        )

        userDao.insert(entity)

        val user = User(
            id = entity.id,
            email = email,
            password = password,
            firstname = firstname,
            lastname = lastname,
            age = age,
            gender = gender,
            bio = bio,
            city = city,
            country = country,
            latitude = null,
            longitude = null,
            maxDistance = 50,
            preferredGender = null,
            photos = photos
        )

        currentUser = user
        return Result.success(user)
    }

    override suspend fun logout() {
        currentUser = null
    }
}
