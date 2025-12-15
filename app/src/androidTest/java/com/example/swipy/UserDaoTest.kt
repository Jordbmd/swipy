package com.example.swipy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swipy.data.local.datasource.AppDatabase
import com.example.swipy.data.local.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

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
    fun insertAndRetrieveUser() = runBlocking {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            password = "password123",
            firstname = "John",
            lastname = "Doe",
            age = 30,
            gender = "male",
            bio = "Test bio",
            city = "Paris",
            country = "France",
            latitude = 48.8566,
            longitude = 2.3522,
            maxDistance = 50,
            preferredGender = "female",
            photos = listOf("photo1.jpg")
        )

        db.userDao().insert(user)

        val retrievedUser = db.userDao().getUserById(1)
        assertNotNull(retrievedUser)
        assertEquals("test@example.com", retrievedUser?.email)
        assertEquals("John", retrievedUser?.firstname)
        assertEquals(30, retrievedUser?.age)
    }

    @Test
    fun getUserByEmailAndPassword() = runBlocking {
        val user = UserEntity(
            id = 2,
            email = "login@example.com",
            password = "mypassword",
            firstname = "Jane",
            lastname = "Smith",
            age = 25,
            gender = "female",
            bio = "",
            city = "",
            country = "",
            latitude = 0.0,
            longitude = 0.0,
            maxDistance = 50,
            preferredGender = "male",
            photos = null
        )

        db.userDao().insert(user)

        val retrievedUser = db.userDao().getUserByEmailAndPassword("login@example.com", "mypassword")
        assertNotNull(retrievedUser)
        assertEquals(2, retrievedUser?.id)

        val wrongPassword = db.userDao().getUserByEmailAndPassword("login@example.com", "wrongpass")
        assertNull(wrongPassword)
    }

    @Test
    fun getUserByEmail() = runBlocking {
        val user = UserEntity(
            id = 3,
            email = "unique@example.com",
            password = "pass",
            firstname = "Bob",
            lastname = "Test",
            age = 35,
            gender = "male",
            bio = "",
            city = "",
            country = "",
            latitude = 0.0,
            longitude = 0.0,
            maxDistance = 50,
            preferredGender = "all",
            photos = null
        )

        db.userDao().insert(user)

        val retrievedUser = db.userDao().getUserByEmail("unique@example.com")
        assertNotNull(retrievedUser)
        assertEquals(3, retrievedUser?.id)

        val nonExistent = db.userDao().getUserByEmail("nonexistent@example.com")
        assertNull(nonExistent)
    }

    @Test
    fun insertAllUsers() = runBlocking {
        val users = listOf(
            UserEntity(10, "user1@test.com", "pass1", "Alice", "A", 20, "female", "", "", "", 0.0, 0.0, 50, "all", null),
            UserEntity(11, "user2@test.com", "pass2", "Bob", "B", 25, "male", "", "", "", 0.0, 0.0, 50, "all", null),
            UserEntity(12, "user3@test.com", "pass3", "Charlie", "C", 30, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        )

        db.userDao().insertAll(users)

        val allUsers = db.userDao().getAllUsers()
        assertEquals(3, allUsers.size)
    }

    @Test
    fun getUserCount() = runBlocking {
        assertEquals(0, db.userDao().getUserCount())

        val user = UserEntity(20, "count@test.com", "pass", "Count", "Test", 20, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        db.userDao().insert(user)

        assertEquals(1, db.userDao().getUserCount())
    }

    @Test
    fun updateUser() = runBlocking {
        val user = UserEntity(30, "update@test.com", "pass", "Original", "Name", 20, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        db.userDao().insert(user)

        val updated = user.copy(firstname = "Updated", age = 25)
        db.userDao().update(updated)

        val retrievedUser = db.userDao().getUserById(30)
        assertEquals("Updated", retrievedUser?.firstname)
        assertEquals(25, retrievedUser?.age)
    }

    @Test
    fun deleteUser() = runBlocking {
        val user = UserEntity(50, "delete@test.com", "pass", "Delete", "Test", 20, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        db.userDao().insert(user)

        assertNotNull(db.userDao().getUserById(50))

        db.userDao().delete(user)

        assertNull(db.userDao().getUserById(50))
    }

    @Test
    fun deleteAllUsers() = runBlocking {
        val users = listOf(
            UserEntity(60, "user1@test.com", "pass", "User", "One", 20, "male", "", "", "", 0.0, 0.0, 50, "all", null),
            UserEntity(61, "user2@test.com", "pass", "User", "Two", 25, "female", "", "", "", 0.0, 0.0, 50, "all", null)
        )
        db.userDao().insertAll(users)

        assertEquals(2, db.userDao().getUserCount())

        db.userDao().deleteAll()

        assertEquals(0, db.userDao().getUserCount())
    }

    @Test
    fun replaceOnConflict() = runBlocking {
        val user = UserEntity(70, "replace@test.com", "pass", "Original", "User", 20, "male", "", "", "", 0.0, 0.0, 50, "all", null)
        db.userDao().insert(user)

        val replacement = UserEntity(70, "replace@test.com", "newpass", "Replaced", "User", 30, "female", "New bio", "", "", 0.0, 0.0, 100, "all", listOf("new.jpg"))
        db.userDao().insert(replacement)

        val retrievedUser = db.userDao().getUserById(70)
        assertEquals("Replaced", retrievedUser?.firstname)
        assertEquals(30, retrievedUser?.age)
        assertEquals("female", retrievedUser?.gender)
        assertEquals("New bio", retrievedUser?.bio)
    }
}
