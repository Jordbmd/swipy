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
    
    suspend fun login(email: String, password: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {

            val response = apiService.getUsers()
            
            if (response.isSuccessful && response.body() != null) {
                val allUsers = response.body()
                
                val user = allUsers?.find {
                    it.email.equals(email, ignoreCase = true) &&
                            it.password == password
                }
                
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        firstname: String,
        lastname: String,
        age: Int,
        gender: String,
        bio: String,
        city: String,
        country: String
    ): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {

            val existingUsers = apiService.getUsers()
            if (existingUsers.isSuccessful && existingUsers.body() != null) {
                val emailExists = existingUsers.body()!!.any { 
                    it.email.equals(email, ignoreCase = true) 
                }
                
                if (emailExists) {
                    return@withContext Result.failure(Exception("Email already registered"))
                }
            }
            
            val newUser = UserResponse(
                id = "",
                email = email,
                password = password,
                firstname = firstname,
                lastname = lastname,
                age = age,
                gender = gender,
                bio = bio,
                city = city,
                country = country,
                latitude = 0.0,
                longitude = 0.0,
                maxDistance = 50,
                preferredGender = "all",
                photos = null
            )
            
            val response = apiService.createUser(newUser)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
