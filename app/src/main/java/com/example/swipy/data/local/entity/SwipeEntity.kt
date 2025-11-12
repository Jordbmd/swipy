package com.example.swipy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "swipes",
    indices = [
        Index(value = ["userId", "targetUserId"], unique = true)
    ]
)
data class SwipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val targetUserId: Int,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

