package com.example.swipy.data.repository

import android.content.Context
import com.example.swipy.data.local.dao.SwipeDao
import com.example.swipy.data.local.dao.UserDao
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.local.entity.UserEntity
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var swipeDao: SwipeDao
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        swipeDao = mockk()
        userDao = mockk()

        mockkStatic("androidx.room.Room")
        val database = mockk<AppDatabase>(relaxed = true)
        every { database.swipeDao() } returns swipeDao
        every { database.userDao() } returns userDao
        every { androidx.room.Room.databaseBuilder(any(), any<Class<AppDatabase>>(), any()) } returns mockk {
            every { build() } returns database
        }
    }

    @Test
    fun `likeUser creates swipe and returns false when no match`() = runTest {
        val userId = 1
        val likedUserId = 2

        coEvery { swipeDao.insert(any()) } just Runs
        coEvery { swipeDao.getLikeSwipe(likedUserId, userId) } returns null
        coEvery { swipeDao.markAsSynced(any(), any()) } just Runs

        val repository = SwipeRepositoryImpl(context)
        val isMatch = repository.likeUser(userId, likedUserId)

        assertFalse(isMatch)

        coVerify { swipeDao.insert(match { it.userId == userId && it.targetUserId == likedUserId && it.action == "like" }) }
        coVerify { swipeDao.getLikeSwipe(likedUserId, userId) }
    }

    @Test
    fun `likeUser creates swipe and returns true when match exists`() = runTest {
        val userId = 1
        val likedUserId = 2
        val reverseSwipe = SwipeEntity(
            id = 10,
            userId = likedUserId,
            targetUserId = userId,
            action = "like",
            timestamp = System.currentTimeMillis(),
            isSynced = true
        )

        coEvery { swipeDao.insert(any()) } just Runs
        coEvery { swipeDao.getLikeSwipe(likedUserId, userId) } returns reverseSwipe
        coEvery { swipeDao.markAsSynced(any(), any()) } just Runs

        val repository = SwipeRepositoryImpl(context)
        val isMatch = repository.likeUser(userId, likedUserId)

        assertTrue(isMatch)

        coVerify { swipeDao.insert(match { it.userId == userId && it.targetUserId == likedUserId && it.action == "like" }) }
        coVerify { swipeDao.getLikeSwipe(likedUserId, userId) }
    }

    @Test
    fun `dislikeUser creates dislike swipe`() = runTest {
        val userId = 1
        val dislikedUserId = 2

        coEvery { swipeDao.insert(any()) } just Runs
        coEvery { swipeDao.markAsSynced(any(), any()) } just Runs

        val repository = SwipeRepositoryImpl(context)
        repository.dislikeUser(userId, dislikedUserId)

        coVerify { swipeDao.insert(match { it.userId == userId && it.targetUserId == dislikedUserId && it.action == "dislike" }) }
    }

    @Test
    fun `getUserSwipes returns list of user swipes`() = runTest {
        val userId = 1
        val swipeEntities = listOf(
            SwipeEntity(1, userId, 2, "like", System.currentTimeMillis(), true),
            SwipeEntity(2, userId, 3, "dislike", System.currentTimeMillis(), true)
        )

        coEvery { swipeDao.getSwipesByUser(userId) } returns swipeEntities

        val repository = SwipeRepositoryImpl(context)
        val swipes = repository.getUserSwipes(userId)

        assertEquals(2, swipes.size)
        assertEquals("like", swipes[0].action)
        assertEquals("dislike", swipes[1].action)

        coVerify { swipeDao.getSwipesByUser(userId) }
    }

    @Test
    fun `getMatches returns users with mutual likes`() = runTest {
        val userId = 1
        val user2 = UserEntity(2, "user2@test.com", "pass", "Alice", "Test", 25, "female", "", "", "", 0.0, 0.0, 50, "all", null)
        val user3 = UserEntity(3, "user3@test.com", "pass", "Bob", "Test", 30, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        
        val userLikes = listOf(
            SwipeEntity(1, userId, 2, "like", System.currentTimeMillis(), true),
            SwipeEntity(2, userId, 3, "like", System.currentTimeMillis(), true)
        )
        
        val reverseLikeUser2 = SwipeEntity(3, 2, userId, "like", System.currentTimeMillis(), true)

        coEvery { swipeDao.getSwipesByAction(userId, "like") } returns userLikes
        coEvery { swipeDao.getLikeSwipe(2, userId) } returns reverseLikeUser2
        coEvery { swipeDao.getLikeSwipe(3, userId) } returns null
        coEvery { userDao.getUserById(2) } returns user2
        coEvery { userDao.getUserById(3) } returns user3

        val repository = SwipeRepositoryImpl(context)
        val matches = repository.getMatches(userId)

        assertEquals(1, matches.size)
        assertEquals(2, matches[0].id)
        assertEquals("Alice", matches[0].firstname)

        coVerify { swipeDao.getSwipesByAction(userId, "like") }
        coVerify { swipeDao.getLikeSwipe(2, userId) }
        coVerify { swipeDao.getLikeSwipe(3, userId) }
        coVerify { userDao.getUserById(2) }
    }

    @Test
    fun `hasSwipedUser returns true when swipe exists`() = runTest {
        val userId = 1
        val targetUserId = 2
        val swipe = SwipeEntity(1, userId, targetUserId, "like", System.currentTimeMillis(), true)

        coEvery { swipeDao.getSwipeByUserAndTarget(userId, targetUserId) } returns swipe

        val repository = SwipeRepositoryImpl(context)
        val hasSwipped = repository.hasSwipedUser(userId, targetUserId)

        assertTrue(hasSwipped)

        coVerify { swipeDao.getSwipeByUserAndTarget(userId, targetUserId) }
    }

    @Test
    fun `hasSwipedUser returns false when no swipe exists`() = runTest {
        val userId = 1
        val targetUserId = 2

        coEvery { swipeDao.getSwipeByUserAndTarget(userId, targetUserId) } returns null

        val repository = SwipeRepositoryImpl(context)
        val hasSwiped = repository.hasSwipedUser(userId, targetUserId)

        assertFalse(hasSwiped)

        coVerify { swipeDao.getSwipeByUserAndTarget(userId, targetUserId) }
    }

    @Test
    fun `getPotentialMatches filters out current user and already swiped users`() = runTest {
        val userId = 1
        val user1 = UserEntity(1, "user1@test.com", "pass", "Current", "User", 25, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        val user2 = UserEntity(2, "user2@test.com", "pass", "Alice", "Test", 25, "female", "", "", "", 0.0, 0.0, 50, "all", null)
        val user3 = UserEntity(3, "user3@test.com", "pass", "Bob", "Test", 30, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        val user4 = UserEntity(4, "user4@test.com", "pass", "Charlie", "Test", 28, "male", "", "", "", 0.0, 0.0, 50, "all", null)

        val allUsers = listOf(user1, user2, user3, user4)
        val userSwipes = listOf(
            SwipeEntity(1, userId, 2, "like", System.currentTimeMillis(), true)
        )

        coEvery { userDao.getAllUsers() } returns allUsers
        coEvery { swipeDao.getSwipesByUser(userId) } returns userSwipes

        val repository = SwipeRepositoryImpl(context)
        val potentialMatches = repository.getPotentialMatches(userId)

        assertEquals(2, potentialMatches.size)
        assertFalse(potentialMatches.any { it.id == userId })
        assertFalse(potentialMatches.any { it.id == 2 })
        assertTrue(potentialMatches.any { it.id == 3 })
        assertTrue(potentialMatches.any { it.id == 4 })

        coVerify { userDao.getAllUsers() }
        coVerify { swipeDao.getSwipesByUser(userId) }
    }

    @Test
    fun `syncPendingSwipes marks swipes as synced`() = runTest {
        val unsyncedSwipes = listOf(
            SwipeEntity(1, 1, 2, "like", System.currentTimeMillis(), false)
        )

        coEvery { swipeDao.getUnsyncedSwipes() } returns unsyncedSwipes
        coEvery { swipeDao.markAsSynced(any(), any()) } just Runs

        val repository = SwipeRepositoryImpl(context)
        repository.syncPendingSwipes()

        coVerify { swipeDao.getUnsyncedSwipes() }
    }
}
