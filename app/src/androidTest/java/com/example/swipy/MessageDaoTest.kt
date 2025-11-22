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
}
