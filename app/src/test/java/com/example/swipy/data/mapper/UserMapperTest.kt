package com.example.swipy.data.mapper

import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.remote.models.UserDto
import org.junit.Test
import org.junit.Assert.*

class UserMapperTest {

    @Test
    fun `UserDto to UserEntity maps correctly with all fields`() {
        val dto = UserDto(
            id = "123",
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
            maxDistance = 100,
            preferredGender = "female",
            photos = "https://example.com/photo.jpg"
        )

        val entity = dto.toEntity()

        assertEquals(123, entity.id)
        assertEquals("test@example.com", entity.email)
        assertEquals("password123", entity.password)
        assertEquals("John", entity.firstname)
        assertEquals("Doe", entity.lastname)
        assertEquals(30, entity.age)
        assertEquals("male", entity.gender)
        assertEquals("Test bio", entity.bio)
        assertEquals("Paris", entity.city)
        assertEquals("France", entity.country)
        assertEquals(48.8566, entity.latitude, 0.0001)
        assertEquals(2.3522, entity.longitude, 0.0001)
        assertEquals(100, entity.maxDistance)
        assertEquals("female", entity.preferredGender)
        assertEquals(listOf("https://example.com/photo.jpg"), entity.photos)
    }

    @Test
    fun `UserDto to UserEntity handles null optional fields`() {
        val dto = UserDto(
            id = "456",
            email = "user@example.com",
            password = "pass",
            firstname = "Jane",
            lastname = "Smith",
            age = 25,
            gender = null,
            bio = null,
            city = null,
            country = null,
            latitude = null,
            longitude = null,
            maxDistance = null,
            preferredGender = null,
            photos = null
        )

        val entity = dto.toEntity()

        assertEquals(456, entity.id)
        assertEquals("other", entity.gender)
        assertEquals("", entity.bio)
        assertEquals("", entity.city)
        assertEquals("", entity.country)
        assertEquals(0.0, entity.latitude, 0.0001)
        assertEquals(0.0, entity.longitude, 0.0001)
        assertEquals(50, entity.maxDistance)
        assertEquals("all", entity.preferredGender)
        assertEquals(emptyList<String>(), entity.photos)
    }

    @Test
    fun `UserDto to UserEntity handles invalid id`() {
        val dto = UserDto(
            id = "invalid",
            email = "test@test.com",
            password = "password",
            firstname = "Test",
            lastname = "User",
            age = 20,
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

        val entity = dto.toEntity()

        assertEquals(0, entity.id)
    }

    @Test
    fun `UserEntity to User handles null photos`() {
        val entity = UserEntity(
            id = 999,
            email = "nullphotos@example.com",
            password = "pass123",
            firstname = "Bob",
            lastname = "Test",
            age = 35,
            gender = "male",
            bio = "Test",
            city = "Marseille",
            country = "France",
            latitude = 43.2965,
            longitude = 5.3698,
            maxDistance = 50,
            preferredGender = "female",
            photos = null
        )

        val user = entity.toUser()

        assertEquals(emptyList<String>(), user.photos)
    }
}
