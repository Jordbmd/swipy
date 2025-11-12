package com.example.swipy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val email: String,
    val password: String,
    val firstname: String,
    val lastname: String,
    val age: Int,
    val gender: String,
    val bio: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val maxDistance: Int,
    val preferredGender: String,
    val photos: List<String>?
)


