package com.example.swipy.data.mapper

import com.example.swipy.data.local.entity.SwipeEntity
import com.example.swipy.data.remote.models.SwipeDto
import com.example.swipy.domain.models.Swipe
import org.junit.Test
import org.junit.Assert.*

class SwipeMapperTest {

    @Test
    fun `SwipeEntity to Swipe maps correctly`() {
        val entity = SwipeEntity(
            id = 1,
            userId = 100,
            targetUserId = 200,
            action = "like",
            timestamp = 123456789L,
            isSynced = true
        )

        val swipe = entity.toSwipe()

        assertEquals(1, swipe.id)
        assertEquals(100, swipe.userId)
        assertEquals(200, swipe.targetUserId)
        assertEquals("like", swipe.action)
        assertEquals(123456789L, swipe.timestamp)
        assertTrue(swipe.isSynced)
    }

    @Test
    fun `Swipe to SwipeEntity maps correctly`() {
        val swipe = Swipe(
            id = 2,
            userId = 300,
            targetUserId = 400,
            action = "dislike",
            timestamp = 987654321L,
            isSynced = false
        )

        val entity = swipe.toEntity()

        assertEquals(2, entity.id)
        assertEquals(300, entity.userId)
        assertEquals(400, entity.targetUserId)
        assertEquals("dislike", entity.action)
        assertEquals(987654321L, entity.timestamp)
        assertFalse(entity.isSynced)
    }

    @Test
    fun `SwipeDto to Swipe maps correctly with valid id`() {
        val dto = SwipeDto(
            id = "5",
            userId = 500,
            targetUserId = 600,
            action = "like",
            timestamp = 111111111L
        )

        val swipe = dto.toSwipe()

        assertEquals(5, swipe.id)
        assertEquals(500, swipe.userId)
        assertEquals(600, swipe.targetUserId)
        assertEquals("like", swipe.action)
        assertEquals(111111111L, swipe.timestamp)
        assertTrue(swipe.isSynced)
    }

    @Test
    fun `SwipeDto to Swipe handles invalid id`() {
        val dto = SwipeDto(
            id = "invalid",
            userId = 700,
            targetUserId = 800,
            action = "dislike",
            timestamp = 222222222L
        )

        val swipe = dto.toSwipe()

        assertEquals(0, swipe.id)
        assertEquals(700, swipe.userId)
        assertEquals(800, swipe.targetUserId)
        assertEquals("dislike", swipe.action)
        assertEquals(222222222L, swipe.timestamp)
        assertTrue(swipe.isSynced)
    }

    @Test
    fun `SwipeDto to SwipeEntity maps correctly`() {
        val dto = SwipeDto(
            id = "10",
            userId = 900,
            targetUserId = 1000,
            action = "like",
            timestamp = 333333333L
        )

        val entity = dto.toEntity()

        assertEquals(10, entity.id)
        assertEquals(900, entity.userId)
        assertEquals(1000, entity.targetUserId)
        assertEquals("like", entity.action)
        assertEquals(333333333L, entity.timestamp)
        assertTrue(entity.isSynced)
    }

    @Test
    fun `Swipe to SwipeDto maps correctly`() {
        val swipe = Swipe(
            id = 15,
            userId = 1100,
            targetUserId = 1200,
            action = "like",
            timestamp = 444444444L,
            isSynced = true
        )

        val dto = swipe.toDto()

        assertEquals("15", dto.id)
        assertEquals(1100, dto.userId)
        assertEquals(1200, dto.targetUserId)
        assertEquals("like", dto.action)
        assertEquals(444444444L, dto.timestamp)
    }
}
