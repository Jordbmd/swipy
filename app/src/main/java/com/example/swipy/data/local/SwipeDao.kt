package com.example.swipy.data.local

import androidx.room.*

@Dao
interface SwipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(swipe: SwipeEntity)

    @Query("SELECT * FROM swipes WHERE userId = :userId AND `action` = :action")
    suspend fun getSwipesByAction(userId: Int, action: String): List<SwipeEntity>

    @Query("SELECT * FROM swipes WHERE userId = :userId AND targetUserId = :targetUserId LIMIT 1")
    suspend fun getSwipe(userId: Int, targetUserId: Int): SwipeEntity?

    @Query("""
        SELECT * FROM swipes 
        WHERE userId = :user1Id 
        AND targetUserId = :user2Id 
        AND `action` = 'like'
    """)
    suspend fun checkMatch(user1Id: Int, user2Id: Int): SwipeEntity?
    
    @Query("SELECT * FROM swipes WHERE userId = :userId AND targetUserId = :targetUserId AND `action` = 'like'")
    suspend fun getLikeSwipe(userId: Int, targetUserId: Int): SwipeEntity?
}
