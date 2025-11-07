package com.example.swipy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.swipy.models.User

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

fun UserEntity.toUser() = User(
    id = id,
    email = email,
    password = password,
    firstname = firstname,
    lastname = lastname,
    age = age,
    gender = gender,
    bio = bio,
    city = city,
    country = country,
    latitude = latitude,
    longitude = longitude,
    maxDistance = maxDistance,
    preferredGender = preferredGender,
    photos = photos ?: emptyList()
)

