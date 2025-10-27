package com.example.swipy.data

import android.content.Context
import androidx.room.Room
import com.example.swipy.data.local.AppDatabase
import com.example.swipy.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseSeeder(private val context: Context) {
    
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    )
        .fallbackToDestructiveMigration()
        .build()
    
    suspend fun seedUsers() = withContext(Dispatchers.IO) {
        val userDao = db.userDao()
        
        // Vérifier si on a déjà des users
        val existingUsers = userDao.getAllUsers()
        if (existingUsers.size >= 5) {
            android.util.Log.d("DatabaseSeeder", "Database already has ${existingUsers.size} users, skipping seed")
            return@withContext
        }
        
        android.util.Log.d("DatabaseSeeder", "Seeding database with test users...")
        
        val testUsers = listOf(
            UserEntity(
                email = "alice@test.com",
                password = "12345678",
                firstname = "Alice",
                lastname = "Martin",
                age = 25,
                gender = "femme",
                bio = "Passionnée de voyages et de photographie 📸",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 50,
                preferredGender = "homme",
                photos = listOf("https://randomuser.me/api/portraits/women/1.jpg")
            ),
            UserEntity(
                email = "bob@test.com",
                password = "12345678",
                firstname = "Bob",
                lastname = "Dupont",
                age = 28,
                gender = "homme",
                bio = "Développeur passionné de tech 💻",
                city = "Lyon",
                country = "France",
                latitude = 45.7640,
                longitude = 4.8357,
                maxDistance = 50,
                preferredGender = "femme",
                photos = listOf("https://randomuser.me/api/portraits/men/1.jpg")
            ),
            UserEntity(
                email = "charlie@test.com",
                password = "12345678",
                firstname = "Charlie",
                lastname = "Bernard",
                age = 30,
                gender = "homme",
                bio = "Amateur de sport et de cuisine 🏃‍♂️🍳",
                city = "Marseille",
                country = "France",
                latitude = 43.2965,
                longitude = 5.3698,
                maxDistance = 50,
                preferredGender = "femme",
                photos = listOf("https://randomuser.me/api/portraits/men/2.jpg")
            ),
            UserEntity(
                email = "diane@test.com",
                password = "12345678",
                firstname = "Diane",
                lastname = "Petit",
                age = 26,
                gender = "femme",
                bio = "Artiste et musicienne 🎨🎵",
                city = "Bordeaux",
                country = "France",
                latitude = 44.8378,
                longitude = -0.5792,
                maxDistance = 50,
                preferredGender = "homme",
                photos = listOf("https://randomuser.me/api/portraits/women/2.jpg")
            ),
            UserEntity(
                email = "emma@test.com",
                password = "12345678",
                firstname = "Emma",
                lastname = "Moreau",
                age = 24,
                gender = "femme",
                bio = "Étudiante en médecine 👩‍⚕️",
                city = "Toulouse",
                country = "France",
                latitude = 43.6047,
                longitude = 1.4442,
                maxDistance = 50,
                preferredGender = "homme",
                photos = listOf("https://randomuser.me/api/portraits/women/3.jpg")
            ),
            UserEntity(
                email = "felix@test.com",
                password = "12345678",
                firstname = "Felix",
                lastname = "Laurent",
                age = 29,
                gender = "homme",
                bio = "Entrepreneur et aventurier 🚀",
                city = "Nice",
                country = "France",
                latitude = 43.7102,
                longitude = 7.2620,
                maxDistance = 50,
                preferredGender = "femme",
                photos = listOf("https://randomuser.me/api/portraits/men/3.jpg")
            )
        )
        
        testUsers.forEach { user ->
            try {
                userDao.insert(user)
                android.util.Log.d("DatabaseSeeder", "Inserted user: ${user.firstname}")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeeder", "Error inserting user ${user.firstname}: ${e.message}")
            }
        }
        
        val totalUsers = userDao.getAllUsers().size
        android.util.Log.d("DatabaseSeeder", "Database now has $totalUsers users")
    }
}
