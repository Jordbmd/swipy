package com.example.swipy.repositories

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String> // userId
    suspend fun register(email: String, password: String): Result<String> // userId
    suspend fun logout()
    fun currentUserIdOrNull(): String?
}
