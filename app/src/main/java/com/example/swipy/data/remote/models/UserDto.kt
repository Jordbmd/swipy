package com.example.swipy.data.remote.models

import com.google.gson.annotations.SerializedName


data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstname")
    val firstname: String,

    @SerializedName("lastname")
    val lastname: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("maxDistance")
    val maxDistance: Int?,

    @SerializedName("preferredGender")
    val preferredGender: String?,

    @SerializedName("photos")
    val photos: String? 
)

