package com.example.swipy.repositories

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.swipy.data.local.AppDatabase
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

    // All users sauf lui même
    suspend fun getPotentialMatches(currentUserId: Int): List<User> {
        val allUsers = userDao.getAllUsers()
        Log.d("UserRepository", "Total users in DB: ${allUsers.size}, current user: $currentUserId")
        return allUsers
            .filter { it.id != currentUserId }
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
    }

    suspend fun likeUser(userId: Int, likedUserId: Int) {
        // TODO: Stocker les likes dans une table dédiée
        Log.d("UserRepository", "User $userId liked user $likedUserId")
    }

    suspend fun dislikeUser(userId: Int, dislikedUserId: Int) {
        // TODO: Stocker les dislikes dans une table dédiée
        Log.d("UserRepository", "User $userId disliked user $dislikedUserId")
    }

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
