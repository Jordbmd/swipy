package com.example.swipy.data.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getOrCreateConversationBetween(userAId: Int, userBId: Int): com.example.swipy.domain.models.Conversation
    fun observeMessages(conversationId: Long): Flow<List<com.example.swipy.domain.models.Message>>
    suspend fun sendMessage(conversationId: Long, senderId: Int, content: String): Result<com.example.swipy.domain.models.Message>
    suspend fun markConversationRead(conversationId: Long, readerId: Int)
    suspend fun getConversationBetween(userAId: Int, userBId: Int): com.example.swipy.domain.models.Conversation?
}
