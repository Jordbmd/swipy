package com.example.swipy.data.repository

import android.content.Context
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.ConversationEntity
import com.example.swipy.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.Room

class ChatRepositoryImpl(context: Context) : ChatRepository {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "swipy-db"
    ).build()

    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()

    override suspend fun getOrCreateConversationBetween(userAId: Int, userBId: Int): com.example.swipy.domain.models.Conversation {
        val existing = conversationDao.getConversationBetween(userAId, userBId)
        if (existing != null) {
            return com.example.swipy.domain.models.Conversation(
                id = existing.id,
                participant1Id = existing.participant1Id,
                participant2Id = existing.participant2Id,
                lastMessageText = existing.lastMessageText,
                lastMessageAt = existing.lastMessageAt,
                unreadCountForP1 = existing.unreadCountForP1,
                unreadCountForP2 = existing.unreadCountForP2,
                createdAt = existing.createdAt
            )
        }

        val convEntity = ConversationEntity(
            participant1Id = userAId,
            participant2Id = userBId,
            createdAt = System.currentTimeMillis()
        )
        val id = conversationDao.insertConversation(convEntity)
        val created = convEntity.copy(id = id)
        return com.example.swipy.domain.models.Conversation(
            id = created.id,
            participant1Id = created.participant1Id,
            participant2Id = created.participant2Id,
            lastMessageText = created.lastMessageText,
            lastMessageAt = created.lastMessageAt,
            unreadCountForP1 = created.unreadCountForP1,
            unreadCountForP2 = created.unreadCountForP2,
            createdAt = created.createdAt
        )
    }

    override fun observeMessages(conversationId: Long): Flow<List<com.example.swipy.domain.models.Message>> {
        return messageDao.observeMessagesForConversation(conversationId).map { list ->
            list.map { me ->
                com.example.swipy.domain.models.Message(
                    id = me.id,
                    conversationId = me.conversationId,
                    senderId = me.senderId,
                    content = me.content,
                    timestamp = me.timestamp,
                    isRead = me.isRead
                )
            }
        }
    }

    override suspend fun sendMessage(conversationId: Long, senderId: Int, content: String): Result<com.example.swipy.domain.models.Message> {
        val entity = MessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            sentStatus = "SENT"
        )
        val id = messageDao.insertMessage(entity)
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            val updated = conv.copy(lastMessageText = content, lastMessageAt = entity.timestamp)
            conversationDao.updateConversation(updated)
        }
        val createdMessage = entity.copy(id = id)
        return Result.success(
            com.example.swipy.domain.models.Message(
                id = createdMessage.id,
                conversationId = createdMessage.conversationId,
                senderId = createdMessage.senderId,
                content = createdMessage.content,
                timestamp = createdMessage.timestamp,
                isRead = createdMessage.isRead
            )
        )
    }

    override suspend fun markConversationRead(conversationId: Long, readerId: Int) {
        messageDao.markMessagesRead(conversationId, readerId)
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            val updated = if (conv.participant1Id == readerId) conv.copy(unreadCountForP1 = 0) else conv.copy(unreadCountForP2 = 0)
            conversationDao.updateConversation(updated)
        }
    }

    override suspend fun getConversationBetween(userAId: Int, userBId: Int): com.example.swipy.domain.models.Conversation? {
        val conv = conversationDao.getConversationBetween(userAId, userBId)
        return conv?.let {
            com.example.swipy.domain.models.Conversation(
                id = it.id,
                participant1Id = it.participant1Id,
                participant2Id = it.participant2Id,
                lastMessageText = it.lastMessageText,
                lastMessageAt = it.lastMessageAt,
                unreadCountForP1 = it.unreadCountForP1,
                unreadCountForP2 = it.unreadCountForP2,
                createdAt = it.createdAt
            )
        }
    }
}
