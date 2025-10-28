package com.example.swipy.data.remote

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("firstname")
    val firstname: String,
    
    @SerializedName("lastname")
    val lastname: String,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("bio")
    val bio: String?,
    
    @SerializedName("city")
    val city: String?,
    
    @SerializedName("country")
    val country: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("photos")
    val photos: List<String>?
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: UserResponse
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("firstname")
    val firstname: String,
    
    @SerializedName("lastname")
    val lastname: String,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: String
)

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: T?
)
