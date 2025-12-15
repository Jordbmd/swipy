package com.example.swipy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.ConversationEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

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
    fun insertAndRetrieveConversation() = runBlocking {
        val conversation = ConversationEntity(
            participant1Id = 1,
            participant2Id = 2,
            lastMessageText = "Hello",
            lastMessageAt = System.currentTimeMillis()
        )

        val conversationId = db.conversationDao().insertConversation(conversation)
        assertTrue(conversationId > 0)

        val retrieved = db.conversationDao().getConversationById(conversationId)
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.participant1Id)
        assertEquals(2, retrieved?.participant2Id)
        assertEquals("Hello", retrieved?.lastMessageText)
    }

    @Test
    fun getConversationBetweenUsers() = runBlocking {
        val conversation = ConversationEntity(
            participant1Id = 10,
            participant2Id = 20
        )

        db.conversationDao().insertConversation(conversation)

        val found1 = db.conversationDao().getConversationBetween(10, 20)
        assertNotNull(found1)
        assertEquals(10, found1?.participant1Id)
        assertEquals(20, found1?.participant2Id)

        val found2 = db.conversationDao().getConversationBetween(20, 10)
        assertNotNull(found2)
        assertEquals(10, found2?.participant1Id)
        assertEquals(20, found2?.participant2Id)

        val notFound = db.conversationDao().getConversationBetween(10, 999)
        assertNull(notFound)
    }

    @Test
    fun updateConversation() = runBlocking {
        val conversation = ConversationEntity(
            participant1Id = 1,
            participant2Id = 2,
            lastMessageText = "Initial message"
        )

        val conversationId = db.conversationDao().insertConversation(conversation)

        val retrieved = db.conversationDao().getConversationById(conversationId)
        assertNotNull(retrieved)

        val updated = retrieved!!.copy(
            lastMessageText = "Updated message",
            lastMessageAt = System.currentTimeMillis(),
            unreadCountForP1 = 5
        )

        val updateCount = db.conversationDao().updateConversation(updated)
        assertEquals(1, updateCount)

        val retrievedAfterUpdate = db.conversationDao().getConversationById(conversationId)
        assertEquals("Updated message", retrievedAfterUpdate?.lastMessageText)
        assertEquals(5, retrievedAfterUpdate?.unreadCountForP1)
    }

    @Test
    fun unreadCounters() = runBlocking {
        val conversation = ConversationEntity(
            participant1Id = 1,
            participant2Id = 2,
            unreadCountForP1 = 3,
            unreadCountForP2 = 7
        )

        val conversationId = db.conversationDao().insertConversation(conversation)

        val retrieved = db.conversationDao().getConversationById(conversationId)
        assertEquals(3, retrieved?.unreadCountForP1)
        assertEquals(7, retrieved?.unreadCountForP2)
    }

    @Test
    fun createdAtTimestamp() = runBlocking {
        val timestamp = System.currentTimeMillis()
        val conversation = ConversationEntity(
            participant1Id = 1,
            participant2Id = 2,
            createdAt = timestamp
        )

        val conversationId = db.conversationDao().insertConversation(conversation)

        val retrieved = db.conversationDao().getConversationById(conversationId)
        assertEquals(timestamp, retrieved?.createdAt)
    }

    @Test
    fun defaultValues() = runBlocking {
        val conversation = ConversationEntity(
            participant1Id = 1,
            participant2Id = 2
        )

        val conversationId = db.conversationDao().insertConversation(conversation)

        val retrieved = db.conversationDao().getConversationById(conversationId)
        assertNull(retrieved?.lastMessageText)
        assertNull(retrieved?.lastMessageAt)
        assertEquals(0, retrieved?.unreadCountForP1)
        assertEquals(0, retrieved?.unreadCountForP2)
        assertTrue((retrieved?.createdAt ?: 0L) > 0)
    }

    @Test
    fun replaceOnConflict() = runBlocking {
        val conversation1 = ConversationEntity(
            id = 100,
            participant1Id = 1,
            participant2Id = 2,
            lastMessageText = "First"
        )

        db.conversationDao().insertConversation(conversation1)

        val conversation2 = ConversationEntity(
            id = 100,
            participant1Id = 1,
            participant2Id = 2,
            lastMessageText = "Replaced"
        )

        db.conversationDao().insertConversation(conversation2)

        val retrieved = db.conversationDao().getConversationById(100)
        assertEquals("Replaced", retrieved?.lastMessageText)
    }
}
