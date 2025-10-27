package com.example.swipy.repositories

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.swipy.data.local.AppDatabase
import com.example.swipy.data.local.SwipeEntity
import com.example.swipy.models.User

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

    suspend fun getPotentialMatches(currentUserId: Int): List<User> {
        val allUsers = userDao.getAllUsers()
        
        // IDs des utilisateurs déjà swipés (likes + dislikes)
        val likedIds = swipeDao.getSwipesByAction(currentUserId, "like").map { it.targetUserId }
        val dislikedIds = swipeDao.getSwipesByAction(currentUserId, "dislike").map { it.targetUserId }
        val swipedIds = likedIds + dislikedIds
        
        Log.d("UserRepository", "=== DEBUG getPotentialMatches ===")
        Log.d("UserRepository", "Current user ID: $currentUserId")
        Log.d("UserRepository", "Total users in DB: ${allUsers.size}")
        Log.d("UserRepository", "All user IDs: ${allUsers.map { it.id }}")
        Log.d("UserRepository", "Already swiped: ${swipedIds.size} users")
        
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
        
        Log.d("UserRepository", "Profiles to show: ${filtered.size}")
        Log.d("UserRepository", "Profile IDs: ${filtered.map { it.id }}")
        
        return filtered
    }

    suspend fun likeUser(userId: Int, likedUserId: Int): Boolean {
        Log.d("UserRepository", "=== LIKE ACTION ===")
        Log.d("UserRepository", "User $userId likes user $likedUserId")
        
        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = likedUserId,
            action = "like"
        )
        swipeDao.insert(swipe)
        Log.d("UserRepository", "Like saved to database")
        
        // Vérifier si l'autre personne nous a aussi liké
        val reverseSwipe = swipeDao.getLikeSwipe(likedUserId, userId)
        Log.d("UserRepository", "Reverse swipe (${likedUserId} → ${userId}): ${reverseSwipe != null}")
        
        if (reverseSwipe != null) {
            Log.d("UserRepository", "🎉🎉🎉 MATCH DETECTED entre $userId et $likedUserId !")
            return true
        }
        
        Log.d("UserRepository", "No match yet")
        return false
    }

    suspend fun dislikeUser(userId: Int, dislikedUserId: Int) {
        val swipe = SwipeEntity(
            userId = userId,
            targetUserId = dislikedUserId,
            action = "dislike"
        )
        swipeDao.insert(swipe)
        Log.d("UserRepository", "User $userId disliked user $dislikedUserId")
    }

    suspend fun getMatches(userId: Int): List<User> {
        Log.d("UserRepository", "=== Getting matches for user $userId ===")
        
        // Récupérer tous les likes de l'utilisateur
        val userLikes = swipeDao.getSwipesByAction(userId, "like")
        Log.d("UserRepository", "User has liked ${userLikes.size} users")
        
        // Pour chaque like, vérifier si c'est un match mutuel
        val matchIds = mutableListOf<Int>()
        for (like in userLikes) {
            val reverseSwipe = swipeDao.getLikeSwipe(like.targetUserId, userId)
            if (reverseSwipe != null) {
                matchIds.add(like.targetUserId)
                Log.d("UserRepository", "Match found with user ${like.targetUserId}")
            }
        }
        
        Log.d("UserRepository", "Total matches: ${matchIds.size}")
        
        // Récupérer les informations complètes des matchs
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
        Log.d("UserRepository", "User $userId updated")
        
        return getUserById(userId)
    }
}

