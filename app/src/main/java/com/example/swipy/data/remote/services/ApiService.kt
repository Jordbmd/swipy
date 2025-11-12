package com.example.swipy.data.remote.services

import com.example.swipy.data.remote.models.CreateSwipeRequest
import com.example.swipy.data.remote.models.SwipeDto
import com.example.swipy.data.remote.models.UserDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserDto>
    
    @POST("users")
    suspend fun createUser(@Body user: UserDto): Response<UserDto>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserDto): Response<UserDto>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>
    
    
    @POST("swipes")
    suspend fun createSwipe(@Body swipe: CreateSwipeRequest): SwipeDto
    
    @GET("swipes")
    suspend fun getAllSwipes(): List<SwipeDto>
    
    @GET("swipes/{id}")
    suspend fun getSwipeById(@Path("id") id: String): SwipeDto
    
    @DELETE("swipes/{id}")
    suspend fun deleteSwipe(@Path("id") id: String): Response<Unit>
}