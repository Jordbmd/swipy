package com.example.swipy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val participant1Id: Int,
    val participant2Id: Int,
    val lastMessageText: String? = null,
    val lastMessageAt: Long? = null,
    val unreadCountForP1: Int = 0,
    val unreadCountForP2: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

