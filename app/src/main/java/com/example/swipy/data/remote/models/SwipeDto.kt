package com.example.swipy.data.remote.models

import com.google.gson.annotations.SerializedName


data class SwipeDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("targetUserId")
    val targetUserId: Int,
    
    @SerializedName("action")
    val action: String,  
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)


data class CreateSwipeRequest(
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("targetUserId")
    val targetUserId: Int,
    
    @SerializedName("action")
    val action: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)


typealias SwipeResponse = SwipeDto