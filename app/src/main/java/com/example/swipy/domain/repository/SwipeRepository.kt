package com.example.swipy.domain.repository

import com.example.swipy.domain.models.Swipe
import com.example.swipy.domain.models.User


interface SwipeRepository {
    
    
    suspend fun likeUser(userId: Int, likedUserId: Int): Boolean
    
    suspend fun dislikeUser(userId: Int, dislikedUserId: Int)
    
    
    suspend fun getUserSwipes(userId: Int): List<Swipe>
    
    
    suspend fun getMatches(userId: Int): List<User>
    
   
    suspend fun syncPendingSwipes()
    
    
    suspend fun syncSwipesFromApi(userId: Int)
    
    
    suspend fun hasSwipedUser(userId: Int, targetUserId: Int): Boolean
    
    
    suspend fun getPotentialMatches(userId: Int): List<User>
}