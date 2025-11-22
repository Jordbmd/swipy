package com.example.swipy.data.local.dao

import androidx.room.*
import com.example.swipy.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesForConversation(conversationId: Long, limit: Int = 100, offset: Int = 0): List<MessageEntity>

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :readerId")
    suspend fun markMessagesRead(conversationId: Long, readerId: Int): Int

    @Query("DELETE FROM messages WHERE conversationId = :id")
    suspend fun deleteMessagesForConversation(id: Long)
}

