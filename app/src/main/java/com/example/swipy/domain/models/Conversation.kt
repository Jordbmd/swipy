package com.example.swipy.domain.models

data class Conversation(
    val id: Long = 0,
    val participant1Id: Int,
    val participant2Id: Int,
    val lastMessageText: String? = null,
    val lastMessageAt: Long? = null,
    val unreadCountForP1: Int = 0,
    val unreadCountForP2: Int = 0,
    val createdAt: Long = 0
)

