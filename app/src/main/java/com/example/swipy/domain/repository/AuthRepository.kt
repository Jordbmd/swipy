package com.example.swipy.domain.repository

import com.example.swipy.domain.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(
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
    ): Result<User>

    suspend fun logout()

    suspend fun currentUserIdOrNull(): String?
    suspend fun currentUserOrNull(): User?
    
  
    suspend fun updateUser(
        userId: Int,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String?,
        city: String?,
        country: String?,
        maxDistance: Int,
        photos: List<String>
    ): User
}