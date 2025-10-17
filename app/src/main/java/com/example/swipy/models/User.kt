package com.example.swipy.models

data class User(
        val id: Int = 0,
        val email: String,
        val password: String,
        val firstname: String,
        val lastname: String,
        val age: Int,
        val gender: String,
        val bio: String? = null,
        val city: String? = null,
        val country: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val maxDistance: Int = 50,
        val preferredGender: String? = null,
        val photos: List<String> = emptyList()
)
