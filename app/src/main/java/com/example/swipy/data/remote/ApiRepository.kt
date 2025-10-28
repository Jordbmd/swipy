package com.example.swipy.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getUsers(): Result<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "getUsers error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: Int): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserById(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "getUserById error", e)
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "login error", e)
            Result.failure(e)
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int,
        gender: String
    ): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(email, password, firstname, lastname, age, gender)
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "register error", e)
            Result.failure(e)
        }
    }
    
    suspend fun createUser(user: UserResponse): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createUser(user)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "createUser error", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: Int, user: UserResponse): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUser(userId, user)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "updateUser error", e)
            Result.failure(e)
        }
    }
}
