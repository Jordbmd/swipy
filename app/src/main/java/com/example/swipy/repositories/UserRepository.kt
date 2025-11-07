package com.example.swipy.repositories

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.swipy.data.local.AppDatabase
import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.remote.ApiRepository
import com.example.swipy.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val userDao = db.userDao()
    private val swipeDao = db.swipeDao()
    
    private val apiRepository = ApiRepository()

    /**
     * Récupère la liste des utilisateurs potentiels à swiper (profils à afficher dans l'écran de swipe).
     * 
     * Étapes :
     * 1. Synchronise les utilisateurs depuis l'API et les sauvegarde en cache local (fonctionne offline si échec)
     * 2. Récupère tous les utilisateurs depuis la base de données locale
     * 3. Récupère les IDs des utilisateurs déjà likés et dislikés
     * 4. Filtre pour exclure :
     *    - L'utilisateur connecté (on ne peut pas se swiper soi-même)
     *    - Les utilisateurs déjà swipés (like ou dislike)
     * 5. Convertit les entités BDD en objets User (format UI)
     * 
     * @param currentUserId L'ID de l'utilisateur connecté
     * @return Liste des utilisateurs à afficher dans l'écran de swipe
     */
    suspend fun getPotentialMatches(currentUserId: Int): List<User> = withContext(Dispatchers.IO) {

        try {
            val apiResult = apiRepository.getUsers()
            
            apiResult.onSuccess { apiUsers ->
                apiUsers.forEach { apiUser ->
                    try {
                        val userId = try {
                            apiUser.id.toInt()
                        } catch (_: NumberFormatException) {
                            return@forEach
                        }
                        
                        val userEntity = UserEntity(
                            id = userId,
                            email = apiUser.email,
                            password = apiUser.password,
                            firstname = apiUser.firstname,
                            lastname = apiUser.lastname,
                            age = apiUser.age,
                            gender = apiUser.gender ?: "other",
                            bio = apiUser.bio,
                            city = apiUser.city,
                            country = apiUser.country,
                            latitude = apiUser.latitude,
                            longitude = apiUser.longitude,
                            maxDistance = apiUser.maxDistance ?: 50,
                            preferredGender = apiUser.preferredGender ?: "all",
                            photos = if (apiUser.photos != null) listOf(apiUser.photos) else emptyList()
                        )
                        userDao.insert(userEntity)
                    } catch (_: Exception) {
                    }
                }
            }.onFailure { error ->
            }
        } catch (_: Exception) {
        }
        
        val allUsers = userDao.getAllUsers()

        val likedIds = swipeDao.getSwipesByAction(currentUserId, "like").map { it.targetUserId }
        val dislikedIds = swipeDao.getSwipesByAction(currentUserId, "dislike").map { it.targetUserId }
        val swipedIds = likedIds + dislikedIds
        

        val filtered = allUsers
            .filter { it.id != currentUserId }
            .filter { it.id !in swipedIds }
            .map { entity ->
                User(
                    id = entity.id,
                    email = entity.email,
                    password = entity.password,
                    firstname = entity.firstname,
                    lastname = entity.lastname,
                    age = entity.age,
                    gender = entity.gender,
                    bio = entity.bio,
                    city = entity.city,
                    country = entity.country,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    maxDistance = entity.maxDistance,
                    preferredGender = entity.preferredGender,
                    photos = entity.photos ?: emptyList()
                )
            }
        
        filtered
    }

    /**
     * Enregistre un like sur un utilisateur et vérifie si c'est un match mutuel.
     * 
     * Étapes :
     * 1. Sauvegarde le like en base de données (userId like likedUserId)
     * 2. Vérifie si l'autre utilisateur a déjà liké l'utilisateur connecté (reverse swipe)
     * 3. Si oui → Match ! Retourne true
     * 4. Si non → Pas de match (pour l'instant). Retourne false
     * 
     * @param userId L'ID de l'utilisateur qui like
     * @param likedUserId L'ID de l'utilisateur qui est liké
     * @return true si c'est un match mutuel, false sinon
     */
    suspend fun likeUser(userId: Int, likedUserId: Int): Boolean {

        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = likedUserId,
            action = "like"
        )
        swipeDao.insert(swipe)

        val reverseSwipe = swipeDao.getLikeSwipe(likedUserId, userId)

        return reverseSwipe != null
    }

    /**
     * Enregistre un dislike sur un utilisateur.
     * 
     * Sauvegarde le dislike en base de données pour ne plus afficher cet utilisateur
     * dans la liste des profils à swiper.
     * 
     * @param userId L'ID de l'utilisateur qui dislike
     * @param dislikedUserId L'ID de l'utilisateur qui est disliké
     */
    suspend fun dislikeUser(userId: Int, dislikedUserId: Int) {
        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = dislikedUserId,
            action = "dislike"
        )
        swipeDao.insert(swipe)
    }

    /**
     * Récupère la liste de tous les matchs de l'utilisateur (likes mutuels).
     * 
     * Étapes :
     * 1. Récupère tous les utilisateurs que l'utilisateur connecté a likés
     * 2. Pour chaque like, vérifie si l'autre personne a également liké l'utilisateur connecté (reverse swipe)
     * 3. Si oui → Ajoute à la liste des matchs
     * 4. Récupère les détails complets des utilisateurs matchés depuis la BDD
     * 5. Convertit en objets User (format UI)
     * 
     * @param userId L'ID de l'utilisateur connecté
     * @return Liste des utilisateurs avec qui l'utilisateur a matché (likes mutuels)
     */
    suspend fun getMatches(userId: Int): List<User> {

        val userLikes = swipeDao.getSwipesByAction(userId, "like")

        val matchIds = mutableListOf<Int>()
        for (like in userLikes) {
            val reverseSwipe = swipeDao.getLikeSwipe(like.targetUserId, userId)
            if (reverseSwipe != null) {
                matchIds.add(like.targetUserId)
            }
        }
        

        val allUsers = userDao.getAllUsers()
        val matches = allUsers
            .filter { it.id in matchIds }
            .map { entity ->
                User(
                    id = entity.id,
                    email = entity.email,
                    password = entity.password,
                    firstname = entity.firstname,
                    lastname = entity.lastname,
                    age = entity.age,
                    gender = entity.gender,
                    bio = entity.bio,
                    city = entity.city,
                    country = entity.country,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    maxDistance = entity.maxDistance,
                    preferredGender = entity.preferredGender,
                    photos = entity.photos ?: emptyList()
                )
            }
        
        return matches
    }
    
    /**
     * Récupère les informations d'un utilisateur spécifique par son ID.
     * 
     * @param userId L'ID de l'utilisateur à récupérer
     * @return L'objet User correspondant, ou null si l'utilisateur n'existe pas en BDD
     */
    suspend fun getUserById(userId: Int): User? {
        val entity = userDao.getUserById(userId)
        if (entity == null) return null
        
        return User(
            id = entity.id,
            email = entity.email,
            password = entity.password,
            firstname = entity.firstname,
            lastname = entity.lastname,
            age = entity.age,
            gender = entity.gender,
            bio = entity.bio,
            city = entity.city,
            country = entity.country,
            latitude = entity.latitude,
            longitude = entity.longitude,
            maxDistance = entity.maxDistance,
            preferredGender = entity.preferredGender,
            photos = entity.photos ?: emptyList()
        )
    }

    /**
     * Met à jour les informations de profil d'un utilisateur.
     * 
     * Étapes :
     * 1. Vérifie que l'utilisateur existe en BDD
     * 2. Crée une copie de l'utilisateur avec les nouvelles informations
     * 3. Sauvegarde les modifications en BDD
     * 4. Récupère et retourne l'utilisateur mis à jour
     * 
     * @param userId L'ID de l'utilisateur à mettre à jour
     * @param firstname Nouveau prénom
     * @param lastname Nouveau nom
     * @param age Nouvel âge
     * @param bio Nouvelle bio
     * @param city Nouvelle ville
     * @param country Nouveau pays
     * @param maxDistance Nouvelle distance maximale de recherche
     * @param photos Nouvelle liste de photos
     * @return L'objet User mis à jour, ou null si l'utilisateur n'existe pas
     */
    suspend fun updateUser(
        userId: Int,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String,
        city: String,
        country: String,
        maxDistance: Int,
        photos: List<String>
    ): User? {
        val existingUser = userDao.getUserById(userId)
        if (existingUser == null) {
            Log.e("UserRepository", "User $userId not found")
            return null
        }
        
        val updatedUser = existingUser.copy(
            firstname = firstname,
            lastname = lastname,
            age = age,
            bio = bio,
            city = city,
            country = country,
            maxDistance = maxDistance,
            photos = photos
        )
        
        userDao.update(updatedUser)

        return getUserById(userId)
    }
}

