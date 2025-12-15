package com.example.swipy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.SwipeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeDaoTest {

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
    fun insertAndRetrieveSwipe() = runBlocking {
        val swipe = SwipeEntity(
            userId = 1,
            targetUserId = 2,
            action = "like",
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )

        db.swipeDao().insert(swipe)

        val swipes = db.swipeDao().getSwipesByUser(1)
        assertEquals(1, swipes.size)
        assertEquals("like", swipes[0].action)
        assertEquals(2, swipes[0].targetUserId)
    }

    @Test
    fun getSwipesByAction() = runBlocking {
        val like1 = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis())
        val like2 = SwipeEntity(userId = 1, targetUserId = 3, action = "like", timestamp = System.currentTimeMillis())
        val dislike = SwipeEntity(userId = 1, targetUserId = 4, action = "dislike", timestamp = System.currentTimeMillis())

        db.swipeDao().insert(like1)
        db.swipeDao().insert(like2)
        db.swipeDao().insert(dislike)

        val likes = db.swipeDao().getSwipesByAction(1, "like")
        assertEquals(2, likes.size)
        assertTrue(likes.all { it.action == "like" })

        val dislikes = db.swipeDao().getSwipesByAction(1, "dislike")
        assertEquals(1, dislikes.size)
        assertEquals("dislike", dislikes[0].action)
    }

    @Test
    fun getLikeSwipe() = runBlocking {
        val like = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis())
        val dislike = SwipeEntity(userId = 3, targetUserId = 4, action = "dislike", timestamp = System.currentTimeMillis())

        db.swipeDao().insert(like)
        db.swipeDao().insert(dislike)

        val foundLike = db.swipeDao().getLikeSwipe(1, 2)
        assertNotNull(foundLike)
        assertEquals(2, foundLike?.targetUserId)

        val notFound = db.swipeDao().getLikeSwipe(3, 4)
        assertNull(notFound)
    }

    @Test
    fun deleteUserSwipes() = runBlocking {
        val swipe1 = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis())
        val swipe2 = SwipeEntity(userId = 1, targetUserId = 3, action = "dislike", timestamp = System.currentTimeMillis())
        val swipe3 = SwipeEntity(userId = 2, targetUserId = 1, action = "like", timestamp = System.currentTimeMillis())

        db.swipeDao().insert(swipe1)
        db.swipeDao().insert(swipe2)
        db.swipeDao().insert(swipe3)

        db.swipeDao().deleteUserSwipes(1)

        val user1Swipes = db.swipeDao().getSwipesByUser(1)
        assertEquals(0, user1Swipes.size)

        val user2Swipes = db.swipeDao().getSwipesByUser(2)
        assertEquals(1, user2Swipes.size)
    }

    @Test
    fun getUserSwipeCount() = runBlocking {
        assertEquals(0, db.swipeDao().getUserSwipeCount(1))

        val swipe1 = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis())
        val swipe2 = SwipeEntity(userId = 1, targetUserId = 3, action = "dislike", timestamp = System.currentTimeMillis())

        db.swipeDao().insert(swipe1)
        db.swipeDao().insert(swipe2)

        assertEquals(2, db.swipeDao().getUserSwipeCount(1))
    }

    @Test
    fun getSwipeByUserAndTarget() = runBlocking {
        val swipe = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis())
        db.swipeDao().insert(swipe)

        val found = db.swipeDao().getSwipeByUserAndTarget(1, 2)
        assertNotNull(found)
        assertEquals("like", found?.action)

        val notFound = db.swipeDao().getSwipeByUserAndTarget(1, 999)
        assertNull(notFound)
    }

    @Test
    fun getUnsyncedSwipes() = runBlocking {
        val synced = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis(), isSynced = true)
        val unsynced1 = SwipeEntity(userId = 1, targetUserId = 3, action = "like", timestamp = System.currentTimeMillis(), isSynced = false)
        val unsynced2 = SwipeEntity(userId = 2, targetUserId = 4, action = "dislike", timestamp = System.currentTimeMillis(), isSynced = false)

        db.swipeDao().insert(synced)
        db.swipeDao().insert(unsynced1)
        db.swipeDao().insert(unsynced2)

        val unsyncedSwipes = db.swipeDao().getUnsyncedSwipes()
        assertEquals(2, unsyncedSwipes.size)
        assertTrue(unsyncedSwipes.all { !it.isSynced })
    }

    @Test
    fun markAsSynced() = runBlocking {
        val swipe = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = System.currentTimeMillis(), isSynced = false)
        db.swipeDao().insert(swipe)

        val unsynced = db.swipeDao().getUnsyncedSwipes()
        assertEquals(1, unsynced.size)

        db.swipeDao().markAsSynced(1, 2)

        val stillUnsynced = db.swipeDao().getUnsyncedSwipes()
        assertEquals(0, stillUnsynced.size)

        val swipeAfterSync = db.swipeDao().getSwipeByUserAndTarget(1, 2)
        assertTrue(swipeAfterSync?.isSynced == true)
    }

    @Test
    fun uniqueConstraintOnUserAndTargetUser() = runBlocking {
        val swipe1 = SwipeEntity(userId = 1, targetUserId = 2, action = "like", timestamp = 111111)
        db.swipeDao().insert(swipe1)

        val swipe2 = SwipeEntity(userId = 1, targetUserId = 2, action = "dislike", timestamp = 222222)
        db.swipeDao().insert(swipe2)

        val swipes = db.swipeDao().getSwipesByUser(1)
        assertEquals(1, swipes.size)
        assertEquals("dislike", swipes[0].action)
        assertEquals(222222L, swipes[0].timestamp)
    }
}
