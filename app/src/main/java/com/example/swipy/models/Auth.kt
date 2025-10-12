package com.example.swipy.models

data class Credentials(val email: String = "", val password: String = "")
data class RegisterData(val email: String = "", val password: String = "", val firstname: String = "", val lastname: String = "", val confirm: String = "")

sealed interface AuthResult {
    data object Loading : AuthResult
    data class Success(val userId: String) : AuthResult
    data class Error(val message: String) : AuthResult
}

object Validators {
    fun email(e: String): Boolean {
        return e.contains("@") && e.contains(".")
    }
    fun password(p: String): Boolean {
        return p.length >= 8
    }
}