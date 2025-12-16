package com.example.swipy.domain.models

data class Credentials(
    val email: String = "",
    val password: String = ""
)

data class RegisterData(
    val email: String = "",
    val password: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val confirm: String = "",
    val age: Int = 18,
    val gender: String = "autre",
    val bio: String = "",
    val city: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photos: List<String> = emptyList()
)


object Validators {
    fun email(e: String): Boolean {
        return e.contains("@") && e.contains(".")
    }

    fun password(p: String): Boolean {
        return p.length >= 8
    }
}
