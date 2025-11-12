package com.example.swipy.domain.models

data class Swipe(
    val id: Int = 0,
    val userId: Int,
    val targetUserId: Int,
    val action: String,  
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)