package com.example.swipy.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<UserResponse>
    
    @POST("users")
    suspend fun createUser(@Body user: UserResponse): Response<UserResponse>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body user: UserResponse
    ): Response<UserResponse>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>
    
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body userData: RegisterRequest): Response<UserResponse>
}
