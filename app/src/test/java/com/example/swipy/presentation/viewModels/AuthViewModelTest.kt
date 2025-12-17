package com.example.swipy.presentation.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swipy.domain.models.Credentials
import com.example.swipy.domain.models.RegisterData
import com.example.swipy.domain.models.User
import com.example.swipy.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials succeeds`() = runTest {
        val credentials = Credentials("test@example.com", "password123")
        val user = User(
            id = 1,
            email = "test@example.com",
            password = "password123",
            firstname = "John",
            lastname = "Doe",
            age = 30,
            gender = "male",
            bio = null,
            city = null,
            country = null,
            latitude = null,
            longitude = null,
            maxDistance = 50,
            preferredGender = null,
            photos = emptyList()
        )

        coEvery { repository.login(any(), any()) } returns Result.success(user)

        viewModel.login(credentials)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(user, state.loggedInUser)

        coVerify { repository.login("test@example.com", "password123") }
    }

    @Test
    fun `login with invalid email shows error`() = runTest {
        val credentials = Credentials("invalid-email", "password123")

        viewModel.login(credentials)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Email invalide", state.error)
        assertNull(state.loggedInUser)

        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login with short password shows error`() = runTest {
        val credentials = Credentials("test@example.com", "short")

        viewModel.login(credentials)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("8 caractères minimum", state.error)
        assertNull(state.loggedInUser)

        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login failure shows error message`() = runTest {
        val credentials = Credentials("test@example.com", "password123")

        coEvery { repository.login(any(), any()) } returns Result.failure(Exception("Login failed"))

        viewModel.login(credentials)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Login failed", state.error)
        assertNull(state.loggedInUser)
    }

    @Test
    fun `register with valid data succeeds`() = runTest {
        val registerData = RegisterData(
            email = "new@example.com",
            password = "password123",
            confirm = "password123",
            firstname = "Jane",
            lastname = "Doe",
            age = 25,
            gender = "female"
        )
        val user = User(
            id = 2,
            email = "new@example.com",
            password = "password123",
            firstname = "Jane",
            lastname = "Doe",
            age = 25,
            gender = "female",
            bio = null,
            city = null,
            country = null,
            latitude = null,
            longitude = null,
            maxDistance = 50,
            preferredGender = null,
            photos = emptyList()
        )

        coEvery { 
            repository.register(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(user)

        viewModel.register(registerData)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(user, state.loggedInUser)
    }

    @Test
    fun `register with invalid email shows error`() = runTest {
        val registerData = RegisterData(
            email = "invalid-email",
            password = "password123",
            confirm = "password123",
            firstname = "Jane",
            lastname = "Doe"
        )

        viewModel.register(registerData)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Email invalide", state.error)
        assertNull(state.loggedInUser)

        coVerify(exactly = 0) { repository.register(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register with mismatched passwords shows error`() = runTest {
        val registerData = RegisterData(
            email = "test@example.com",
            password = "password123",
            confirm = "different",
            firstname = "Jane",
            lastname = "Doe"
        )

        viewModel.register(registerData)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Les mots de passe ne correspondent pas", state.error)
        assertNull(state.loggedInUser)

        coVerify(exactly = 0) { repository.register(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register failure shows error message`() = runTest {
        val registerData = RegisterData(
            email = "test@example.com",
            password = "password123",
            confirm = "password123",
            firstname = "Jane",
            lastname = "Doe"
        )

        coEvery { 
            repository.register(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Email déjà utilisé"))

        viewModel.register(registerData)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Email déjà utilisé", state.error)
        assertNull(state.loggedInUser)
    }

    @Test
    fun `logout clears user state`() = runTest {
        coEvery { repository.logout() } returns Unit

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.loggedInUser)

        coVerify { repository.logout() }
    }
}
