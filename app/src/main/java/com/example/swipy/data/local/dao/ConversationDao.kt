package com.example.swipy.data.local.dao

import androidx.room.*
import com.example.swipy.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity): Int

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE (participant1Id = :a AND participant2Id = :b) OR (participant1Id = :b AND participant2Id = :a) LIMIT 1")
    suspend fun getConversationBetween(a: Int, b: Int): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE participant1Id = :userId OR participant2Id = :userId ORDER BY lastMessageAt DESC")
    fun observeConversationsForUser(userId: Int): Flow<List<ConversationEntity>>
}

