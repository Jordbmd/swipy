package com.example.swipy.repositories

import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FakeAuthRepository : AuthRepository {
    private val users = ConcurrentHashMap<String, String>() // email -> password
    @Volatile private var currentUserId: String? = null

    override fun currentUserIdOrNull() = currentUserId

    override suspend fun login(email: String, password: String): Result<String> {
        delay(400)
        val stored = users[email] ?: return Result.failure(IllegalArgumentException("Email inconnu"))
        if (stored != password) return Result.failure(IllegalArgumentException("Mot de passe incorrect"))
        val id = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        currentUserId = id
        return Result.success(id)
    }

    override suspend fun register(email: String, password: String): Result<String> {
        delay(600)
        if (users.containsKey(email)) return Result.failure(IllegalStateException("Email déjà utilisé"))
        users[email] = password
        val id = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        currentUserId = id
        return Result.success(id)
    }

    override suspend fun logout() { currentUserId = null }
}
