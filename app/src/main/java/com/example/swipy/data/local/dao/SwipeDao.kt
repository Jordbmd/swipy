package com.example.swipy.data.local.dao

import androidx.room.*
import com.example.swipy.data.local.entity.SwipeEntity

@Dao
interface SwipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(swipe: SwipeEntity)

    @Query("SELECT * FROM swipes WHERE userId = :userId AND action = :action")
    suspend fun getSwipesByAction(userId: Int, action: String): List<SwipeEntity>

    @Query("SELECT * FROM swipes WHERE userId = :userId AND targetUserId = :targetUserId AND action = 'like' LIMIT 1")
    suspend fun getLikeSwipe(userId: Int, targetUserId: Int): SwipeEntity?

    @Query("DELETE FROM swipes WHERE userId = :userId")
    suspend fun deleteUserSwipes(userId: Int)

    @Query("SELECT COUNT(*) FROM swipes WHERE userId = :userId")
    suspend fun getUserSwipeCount(userId: Int): Int
}

