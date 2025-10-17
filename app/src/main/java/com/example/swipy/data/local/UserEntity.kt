package com.example.swipy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val firstname: String,
    val lastname: String,
    val age: Int,
    val gender: String,
    val bio: String?,
    val city: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val maxDistance: Int,
    val preferredGender: String?,
    val photos: List<String>?
)

