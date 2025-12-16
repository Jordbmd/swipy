package com.example.swipy.data.remote

import android.util.Log
import com.example.swipy.data.remote.models.UserDto
import com.example.swipy.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRemoteDataSource {
    
    private val apiService = RetrofitInstance.apiService
    

    suspend fun getUsers(): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<UserDto> = withContext(Dispatchers.IO) {
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
        country: String,
        latitude: Double,
        longitude: Double,
        photos: String? = null
    ): Result<UserDto> = withContext(Dispatchers.IO) {
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
            
            val newUser = UserDto(
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
                latitude = latitude,
                longitude = longitude,
                maxDistance = 50,
                preferredGender = "all",
                photos = photos
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
    
   
    suspend fun updateUser(
        userId: String,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String,
        city: String,
        country: String,
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Int? = null,
        gender: String? = null,
        preferredGender: String? = null,
        photos: String? = null
    ): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val currentUserResponse = apiService.getUserById(userId)
            
            if (!currentUserResponse.isSuccessful || currentUserResponse.body() == null) {
                return@withContext Result.failure(Exception("User not found"))
            }
            
            val currentUser = currentUserResponse.body()!!
            
            val updatedUser = currentUser.copy(
                firstname = firstname,
                lastname = lastname,
                age = age,
                bio = bio,
                city = city,
                country = country,
                latitude = latitude ?: currentUser.latitude,
                longitude = longitude ?: currentUser.longitude,
                maxDistance = maxDistance ?: currentUser.maxDistance,
                gender = gender ?: currentUser.gender,
                preferredGender = preferredGender ?: currentUser.preferredGender,
                photos = photos ?: currentUser.photos
            )
            
            val response = apiService.updateUser(userId, updatedUser)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
