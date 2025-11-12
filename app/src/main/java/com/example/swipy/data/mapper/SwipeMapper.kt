package com.example.swipy.data.mapper

import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.remote.models.SwipeDto
import com.example.swipy.domain.models.Swipe


fun SwipeEntity.toSwipe(): Swipe {
    return Swipe(
        id = this.id,
        userId = this.userId,
        targetUserId = this.targetUserId,
        action = this.action,
        timestamp = this.timestamp,
        isSynced = this.isSynced
    )
}


fun Swipe.toEntity(): SwipeEntity {
    return SwipeEntity(
        id = this.id,
        userId = this.userId,
        targetUserId = this.targetUserId,
        action = this.action,
        timestamp = this.timestamp,
        isSynced = this.isSynced
    )
}


fun SwipeDto.toSwipe(): Swipe {
    return Swipe(
        id = this.id.toIntOrNull() ?: 0,
        userId = this.userId,
        targetUserId = this.targetUserId,
        action = this.action,
        timestamp = this.timestamp,
        isSynced = true 
    )
}


fun SwipeDto.toEntity(): SwipeEntity {
    return SwipeEntity(
        id = this.id.toIntOrNull() ?: 0,
        userId = this.userId,
        targetUserId = this.targetUserId,
        action = this.action,
        timestamp = this.timestamp,
        isSynced = true  
    )
}


fun Swipe.toDto(): SwipeDto {
    return SwipeDto(
        id = this.id.toString(),
        userId = this.userId,
        targetUserId = this.targetUserId,
        action = this.action,
        timestamp = this.timestamp
    )
}