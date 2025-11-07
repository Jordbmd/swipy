package com.example.swipy.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>
    @POST("users")
    suspend fun createUser(@Body user: UserResponse): Response<UserResponse>

}
