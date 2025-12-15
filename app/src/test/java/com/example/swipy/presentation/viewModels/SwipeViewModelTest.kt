package com.example.swipy.presentation.viewModels

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.SwipeRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SwipeRepositoryImpl
    private lateinit var viewModel: SwipeViewModel

    private val testUser1 = User(
        id = 1,
        email = "user1@test.com",
        password = "pass",
        firstname = "Alice",
        lastname = "Test",
        age = 25,
        gender = "female"
    )

    private val testUser2 = User(
        id = 2,
        email = "user2@test.com",
        password = "pass",
        firstname = "Bob",
        lastname = "Test",
        age = 30,
        gender = "male"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfiles loads potential matches`() = runTest {
        val profiles = listOf(testUser1, testUser2)
        coEvery { repository.getPotentialMatches(100) } returns profiles

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(profiles, state.profiles)
        assertEquals(0, state.currentProfileIndex)
        assertNull(state.error)

        coVerify { repository.getPotentialMatches(100) }
    }

    @Test
    fun `swipeRight without match moves to next profile`() = runTest {
        val profiles = listOf(testUser1, testUser2)
        coEvery { repository.getPotentialMatches(100) } returns profiles
        coEvery { repository.likeUser(100, 1) } returns false

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        viewModel.swipeRight()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.currentProfileIndex)
        assertNull(state.matchedUser)

        coVerify { repository.likeUser(100, 1) }
    }

    @Test
    fun `swipeRight with match sets matchedUser`() = runTest {
        val profiles = listOf(testUser1, testUser2)
        coEvery { repository.getPotentialMatches(100) } returns profiles
        coEvery { repository.likeUser(100, 1) } returns true

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        viewModel.swipeRight()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.currentProfileIndex)
        assertEquals(testUser1, state.matchedUser)

        coVerify { repository.likeUser(100, 1) }
    }

    @Test
    fun `swipeLeft dislikes user and moves to next profile`() = runTest {
        val profiles = listOf(testUser1, testUser2)
        coEvery { repository.getPotentialMatches(100) } returns profiles
        coEvery { repository.dislikeUser(100, 1) } returns Unit

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        viewModel.swipeLeft()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.currentProfileIndex)

        coVerify { repository.dislikeUser(100, 1) }
    }

    @Test
    fun `clearMatch removes matched user from state`() = runTest {
        val profiles = listOf(testUser1)
        coEvery { repository.getPotentialMatches(100) } returns profiles
        coEvery { repository.likeUser(100, 1) } returns true

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        viewModel.swipeRight()
        advanceUntilIdle()

        assertEquals(testUser1, viewModel.state.value.matchedUser)

        viewModel.clearMatch()

        assertNull(viewModel.state.value.matchedUser)
    }

    @Test
    fun `getCurrentProfile returns current profile when available`() = runTest {
        val profiles = listOf(testUser1, testUser2)
        coEvery { repository.getPotentialMatches(100) } returns profiles

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        val currentProfile = viewModel.getCurrentProfile()

        assertEquals(testUser1, currentProfile)
    }

    @Test
    fun `getCurrentProfile returns null when no profiles left`() = runTest {
        val profiles = listOf(testUser1)
        coEvery { repository.getPotentialMatches(100) } returnsMany listOf(profiles, emptyList())
        coEvery { repository.likeUser(100, 1) } returns false

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        // Swipe through the only profile
        viewModel.swipeRight()
        advanceUntilIdle()

        val currentProfile = viewModel.getCurrentProfile()

        assertNull(currentProfile)
    }

    @Test
    fun `swiping through all profiles reloads profiles`() = runTest {
        val firstBatch = listOf(testUser1)
        val secondBatch = listOf(testUser2)
        
        coEvery { repository.getPotentialMatches(100) } returnsMany listOf(firstBatch, secondBatch)
        coEvery { repository.likeUser(any(), any()) } returns false

        viewModel = SwipeViewModel(repository, 100)
        advanceUntilIdle()

        assertEquals(testUser1, viewModel.getCurrentProfile())

        viewModel.swipeRight()
        advanceUntilIdle()

        assertEquals(testUser2, viewModel.getCurrentProfile())

        coVerify(exactly = 2) { repository.getPotentialMatches(100) }
    }
}
