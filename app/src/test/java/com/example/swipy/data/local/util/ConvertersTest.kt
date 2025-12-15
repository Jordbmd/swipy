package com.example.swipy.data.local.util

import org.junit.Test
import org.junit.Assert.*

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromList converts list to comma-separated string`() {
        val list = listOf("photo1.jpg", "photo2.jpg", "photo3.jpg")
        val result = converters.fromList(list)
        assertEquals("photo1.jpg,photo2.jpg,photo3.jpg", result)
    }

    @Test
    fun `fromList returns null for null input`() {
        val result = converters.fromList(null)
        assertNull(result)
    }

    @Test
    fun `fromList returns empty string for empty list`() {
        val result = converters.fromList(emptyList())
        assertEquals("", result)
    }

    @Test
    fun `toList converts comma-separated string to list`() {
        val data = "photo1.jpg,photo2.jpg,photo3.jpg"
        val result = converters.toList(data)
        assertEquals(listOf("photo1.jpg", "photo2.jpg", "photo3.jpg"), result)
    }

    @Test
    fun `toList returns null for null input`() {
        val result = converters.toList(null)
        assertNull(result)
    }

    @Test
    fun `toList filters empty strings`() {
        val data = "photo1.jpg,,photo2.jpg"
        val result = converters.toList(data)
        assertEquals(listOf("photo1.jpg", "photo2.jpg"), result)
    }

    @Test
    fun `toList returns null for empty string`() {
        val result = converters.toList("")
        assertEquals(emptyList<String>(), result)
    }
}
