package com.example.swipy.data.remote

import android.util.Log
import com.example.swipy.data.remote.models.CreateSwipeRequest
import com.example.swipy.data.remote.models.SwipeDto
import com.example.swipy.data.remote.services.ApiService
import com.example.swipy.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SwipeRemoteDataSource {
    
    private val apiService: ApiService = RetrofitInstance.apiService

    suspend fun createSwipe(
        userId: Int,
        targetUserId: Int,
        action: String
    ): Result<SwipeDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateSwipeRequest(
                userId = userId,
                targetUserId = targetUserId,
                action = action,
                timestamp = System.currentTimeMillis()
            )
            
            val response = apiService.createSwipe(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSwipesByUser(userId: Int): Result<List<SwipeDto>> = withContext(Dispatchers.IO) {
        try {
            val allSwipes = apiService.getAllSwipes()
            val userSwipes = allSwipes.filter { it.userId == userId }
            Result.success(userSwipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSwipes(): Result<List<SwipeDto>> = withContext(Dispatchers.IO) {
        try {
            val swipes = apiService.getAllSwipes()
            Result.success(swipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}