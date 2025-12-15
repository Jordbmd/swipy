package com.example.swipy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.ConversationEntity
import com.example.swipy.data.local.entity.MessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveMessage() = runBlocking {
        val convId = db.conversationDao().insertConversation(
            ConversationEntity(participant1Id = 1, participant2Id = 2)
        )

        val msg = MessageEntity(conversationId = convId, senderId = 1, content = "Salut")
        val msgId = db.messageDao().insertMessage(msg)

        val messages = db.messageDao().getMessagesForConversation(convId)
        assertEquals(1, messages.size)
        assertEquals("Salut", messages[0].content)
        assertEquals(msgId, messages[0].id)
    }

    @Test
    fun getMessagesForConversationOrderedByTimestamp() = runBlocking {
        val convId = db.conversationDao().insertConversation(
            ConversationEntity(participant1Id = 1, participant2Id = 2)
        )

        val msg1 = MessageEntity(conversationId = convId, senderId = 1, content = "First", timestamp = 1000)
        val msg2 = MessageEntity(conversationId = convId, senderId = 2, content = "Second", timestamp = 2000)
        val msg3 = MessageEntity(conversationId = convId, senderId = 1, content = "Third", timestamp = 3000)

        db.messageDao().insertMessage(msg3)
        db.messageDao().insertMessage(msg1)
        db.messageDao().insertMessage(msg2)

        val messages = db.messageDao().getMessagesForConversation(convId)
        assertEquals(3, messages.size)
        assertEquals("First", messages[0].content)
        assertEquals("Second", messages[1].content)
        assertEquals("Third", messages[2].content)
    }

    @Test
    fun getMessagesWithLimitAndOffset() = runBlocking {
        val convId = db.conversationDao().insertConversation(
            ConversationEntity(participant1Id = 1, participant2Id = 2)
        )

        for (i in 1..10) {
            db.messageDao().insertMessage(
                MessageEntity(conversationId = convId, senderId = 1, content = "Message $i", timestamp = i.toLong())
            )
        }

        val firstPage = db.messageDao().getMessagesForConversation(convId, limit = 3, offset = 0)
        assertEquals(3, firstPage.size)
        assertEquals("Message 1", firstPage[0].content)

        val secondPage = db.messageDao().getMessagesForConversation(convId, limit = 3, offset = 3)
        assertEquals(3, secondPage.size)
        assertEquals("Message 4", secondPage[0].content)
    }

    @Test
    fun markMessagesRead() = runBlocking {
        val convId = db.conversationDao().insertConversation(
            ConversationEntity(participant1Id = 1, participant2Id = 2)
        )

        val msg1 = MessageEntity(conversationId = convId, senderId = 1, content = "From sender 1", isRead = false)
        val msg2 = MessageEntity(conversationId = convId, senderId = 2, content = "From sender 2", isRead = false)
        val msg3 = MessageEntity(conversationId = convId, senderId = 2, content = "Also from sender 2", isRead = false)

        db.messageDao().insertMessage(msg1)
        db.messageDao().insertMessage(msg2)
        db.messageDao().insertMessage(msg3)

        val markedCount = db.messageDao().markMessagesRead(convId, readerId = 1)
        assertEquals(2, markedCount)

        val messages = db.messageDao().getMessagesForConversation(convId)
        assertEquals(false, messages[0].isRead)
        assertEquals(true, messages[1].isRead)
        assertEquals(true, messages[2].isRead)
    }

    @Test
    fun deleteMessagesForConversation() = runBlocking {
        val conv1 = db.conversationDao().insertConversation(ConversationEntity(participant1Id = 1, participant2Id = 2))
        val conv2 = db.conversationDao().insertConversation(ConversationEntity(participant1Id = 3, participant2Id = 4))

        db.messageDao().insertMessage(MessageEntity(conversationId = conv1, senderId = 1, content = "Conv1 Msg1"))
        db.messageDao().insertMessage(MessageEntity(conversationId = conv1, senderId = 2, content = "Conv1 Msg2"))
        db.messageDao().insertMessage(MessageEntity(conversationId = conv2, senderId = 3, content = "Conv2 Msg1"))

        db.messageDao().deleteMessagesForConversation(conv1)

        val conv1Messages = db.messageDao().getMessagesForConversation(conv1)
        assertEquals(0, conv1Messages.size)

        val conv2Messages = db.messageDao().getMessagesForConversation(conv2)
        assertEquals(1, conv2Messages.size)
    }
}
