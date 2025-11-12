package com.example.swipy.data.mapper

import com.example.swipy.data.local.entity.UserEntity
import com.example.swipy.data.remote.models.UserDto
import com.example.swipy.domain.models.User


fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = this.id.toIntOrNull() ?: 0,
        email = this.email,
        password = this.password,
        firstname = this.firstname,
        lastname = this.lastname,
        age = this.age,
        gender = this.gender ?: "other",
        bio = this.bio ?: "",
        city = this.city ?: "",
        country = this.country ?: "",
        latitude = this.latitude ?: 0.0,
        longitude = this.longitude ?: 0.0,
        maxDistance = this.maxDistance ?: 50,
        preferredGender = this.preferredGender ?: "all",
        photos = if (this.photos != null) listOf(this.photos) else emptyList()
    )
}



fun UserEntity.toUser(): User {
    return User(
        id = this.id,
        email = this.email,
        password = this.password,
        firstname = this.firstname,
        lastname = this.lastname,
        age = this.age,
        gender = this.gender,
        bio = this.bio,
        city = this.city,
        country = this.country,
        latitude = this.latitude,
        longitude = this.longitude,
        maxDistance = this.maxDistance,
        preferredGender = this.preferredGender,
        photos = this.photos ?: emptyList()
    )
}