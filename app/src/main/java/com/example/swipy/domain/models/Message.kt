package com.example.swipy.domain.models

data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val senderId: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

