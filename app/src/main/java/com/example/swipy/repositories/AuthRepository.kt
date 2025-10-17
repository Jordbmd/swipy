package com.example.swipy.repositories

import com.example.swipy.models.User

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

    fun currentUserIdOrNull(): String?
    fun currentUserOrNull(): User?
}
