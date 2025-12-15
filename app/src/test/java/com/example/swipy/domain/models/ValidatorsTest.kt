package com.example.swipy.domain.models

import org.junit.Test
import org.junit.Assert.*

class ValidatorsTest {

    @Test
    fun `email validator returns true for valid email`() {
        assertTrue(Validators.email("test@example.com"))
        assertTrue(Validators.email("user@domain.co.uk"))
        assertTrue(Validators.email("name.surname@company.org"))
    }

    @Test
    fun `email validator returns false for invalid email without @`() {
        assertFalse(Validators.email("testexample.com"))
    }

    @Test
    fun `email validator returns false for invalid email without dot`() {
        assertFalse(Validators.email("test@examplecom"))
    }

    @Test
    fun `email validator returns false for empty string`() {
        assertFalse(Validators.email(""))
    }

    @Test
    fun `password validator returns true for password with 8 or more characters`() {
        assertTrue(Validators.password("12345678"))
        assertTrue(Validators.password("password123"))
        assertTrue(Validators.password("a".repeat(8)))
    }

    @Test
    fun `password validator returns false for password with less than 8 characters`() {
        assertFalse(Validators.password("1234567"))
        assertFalse(Validators.password("short"))
        assertFalse(Validators.password(""))
    }
}
