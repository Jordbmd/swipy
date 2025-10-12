package com.example.swipy.repositories

import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.example.swipy.models.User

class FakeAuthRepository : AuthRepository {
    private data class UserRecord(
        val id: String,
        val email: String,
        val password: String,
        val firstname: String,
        val lastname: String
    )
    private val users = ConcurrentHashMap<String, UserRecord>() // email -> password
    @Volatile private var currentUserId: String? = null

    override fun currentUserIdOrNull() = currentUserId

    override fun currentUserOrNull(): User? {
        val id = currentUserIdOrNull()
        if (id == null) {
            return null
        }
        val rec = users.values.firstOrNull { it.id == id }
        if (rec == null) {
            return null
        }
        return User(rec.id, rec.email, rec.firstname, rec.lastname)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        delay(400)
        val stored = users[email]
        if (stored == null) {
            return Result.failure(IllegalArgumentException("Email inconnu"))
        }
        if (stored.password != password) {
            return Result.failure(IllegalArgumentException("Mot de passe incorrect"))
        }
        currentUserId = stored.id
        return Result.success(User(stored.id, email, stored.firstname, stored.lastname))
    }

    override suspend fun register(email: String, password: String, firstname: String, lastname: String): Result<User> {
        delay(600)
        if (users.containsKey(email)) {
            return Result.failure(IllegalStateException("Email déjà utilisé"))
        }
        val id = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
        val stored = UserRecord(id, email, password, firstname, lastname)
        users[email] = stored
        currentUserId = id
        return Result.success(User(stored.id, stored.email, stored.firstname, stored.lastname))
    }

    override suspend fun logout() { currentUserId = null }
}
