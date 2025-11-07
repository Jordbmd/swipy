package com.example.swipy.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.swipy.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseSeeder(context: Context) {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    ).fallbackToDestructiveMigration().build()

    private val userDao = db.userDao()

    suspend fun seedDatabase(forceReseed: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val userCount = userDao.getUserCount()
            
            if (userCount > 0 && !forceReseed) {
                Log.d("DatabaseSeeder", "Database already seeded with $userCount users")
                return@withContext
            }
            
            if (forceReseed) {
                Log.d("DatabaseSeeder", "Force reseeding: deleting existing users")
                userDao.deleteAll()
            }

            Log.d("DatabaseSeeder", "Seeding database with demo users...")

            val demoUsers = listOf(
                UserEntity(
                    id = 1,
                    email = "alice@example.com",
                    password = "password123",
                    firstname = "Alice",
                    lastname = "Dubois",
                    age = 25,
                    gender = "female",
                    bio = "Passionnée de voyage et de photographie",
                    city = "Paris",
                    country = "France",
                    latitude = 48.8566,
                    longitude = 2.3522,
                    maxDistance = 50,
                    preferredGender = "male",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=1",
                        "https://i.pravatar.cc/300?img=5"
                    )
                ),
                UserEntity(
                    id = 2,
                    email = "bob@example.com",
                    password = "password123",
                    firstname = "Bob",
                    lastname = "Martin",
                    age = 28,
                    gender = "male",
                    bio = "Développeur passionné, j'adore le sport",
                    city = "Lyon",
                    country = "France",
                    latitude = 45.7640,
                    longitude = 4.8357,
                    maxDistance = 30,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=12",
                        "https://i.pravatar.cc/300?img=13"
                    )
                ),
                UserEntity(
                    id = 3,
                    email = "claire@example.com",
                    password = "password123",
                    firstname = "Claire",
                    lastname = "Bernard",
                    age = 23,
                    gender = "female",
                    bio = "Étudiante en design, fan de musique indie",
                    city = "Marseille",
                    country = "France",
                    latitude = 43.2965,
                    longitude = 5.3698,
                    maxDistance = 40,
                    preferredGender = "male",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=9",
                        "https://i.pravatar.cc/300?img=10"
                    )
                ),
                UserEntity(
                    id = 4,
                    email = "david@example.com",
                    password = "password123",
                    firstname = "David",
                    lastname = "Petit",
                    age = 30,
                    gender = "male",
                    bio = "Chef cuisinier, amateur de vin",
                    city = "Bordeaux",
                    country = "France",
                    latitude = 44.8378,
                    longitude = -0.5792,
                    maxDistance = 60,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=15",
                        "https://i.pravatar.cc/300?img=16"
                    )
                ),
                UserEntity(
                    id = 5,
                    email = "emma@example.com",
                    password = "password123",
                    firstname = "Emma",
                    lastname = "Roux",
                    age = 26,
                    gender = "female",
                    bio = "Architecte d'intérieur, amoureuse de la nature",
                    city = "Toulouse",
                    country = "France",
                    latitude = 43.6047,
                    longitude = 1.4442,
                    maxDistance = 45,
                    preferredGender = "male",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=20",
                        "https://i.pravatar.cc/300?img=21"
                    )
                ),
                UserEntity(
                    id = 6,
                    email = "felix@example.com",
                    password = "password123",
                    firstname = "Félix",
                    lastname = "Moreau",
                    age = 27,
                    gender = "male",
                    bio = "Photographe freelance, toujours en vadrouille",
                    city = "Nice",
                    country = "France",
                    latitude = 43.7102,
                    longitude = 7.2620,
                    maxDistance = 35,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=22",
                        "https://i.pravatar.cc/300?img=23"
                    )
                ),
                UserEntity(
                    id = 7,
                    email = "julie@example.com",
                    password = "password123",
                    firstname = "Julie",
                    lastname = "Simon",
                    age = 24,
                    gender = "female",
                    bio = "Professeure de yoga, végétarienne",
                    city = "Nantes",
                    country = "France",
                    latitude = 47.2184,
                    longitude = -1.5536,
                    maxDistance = 50,
                    preferredGender = "all",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=25",
                        "https://i.pravatar.cc/300?img=26"
                    )
                ),
                UserEntity(
                    id = 8,
                    email = "lucas@example.com",
                    password = "password123",
                    firstname = "Lucas",
                    lastname = "Laurent",
                    age = 29,
                    gender = "male",
                    bio = "Ingénieur en énergie renouvelable",
                    city = "Strasbourg",
                    country = "France",
                    latitude = 48.5734,
                    longitude = 7.7521,
                    maxDistance = 40,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=30",
                        "https://i.pravatar.cc/300?img=31"
                    )
                ),
                UserEntity(
                    id = 9,
                    email = "marie@example.com",
                    password = "password123",
                    firstname = "Marie",
                    lastname = "Lefevre",
                    age = 22,
                    gender = "female",
                    bio = "Étudiante en médecine, joueuse de piano",
                    city = "Rennes",
                    country = "France",
                    latitude = 48.1173,
                    longitude = -1.6778,
                    maxDistance = 30,
                    preferredGender = "male",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=35",
                        "https://i.pravatar.cc/300?img=36"
                    )
                ),
                UserEntity(
                    id = 10,
                    email = "nathan@example.com",
                    password = "password123",
                    firstname = "Nathan",
                    lastname = "Garnier",
                    age = 31,
                    gender = "male",
                    bio = "Entrepreneur dans la tech, passionné d'escalade",
                    city = "Lille",
                    country = "France",
                    latitude = 50.6292,
                    longitude = 3.0573,
                    maxDistance = 70,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=40",
                        "https://i.pravatar.cc/300?img=41"
                    )
                ),
                UserEntity(
                    id = 11,
                    email = "sophie@example.com",
                    password = "password123",
                    firstname = "Sophie",
                    lastname = "Rousseau",
                    age = 27,
                    gender = "female",
                    bio = "Journaliste, passionnée de littérature",
                    city = "Montpellier",
                    country = "France",
                    latitude = 43.6108,
                    longitude = 3.8767,
                    maxDistance = 55,
                    preferredGender = "male",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=45",
                        "https://i.pravatar.cc/300?img=46"
                    )
                ),
                UserEntity(
                    id = 12,
                    email = "theo@example.com",
                    password = "password123",
                    firstname = "Théo",
                    lastname = "Vincent",
                    age = 26,
                    gender = "male",
                    bio = "Musicien, guitariste dans un groupe de rock",
                    city = "Grenoble",
                    country = "France",
                    latitude = 45.1885,
                    longitude = 5.7245,
                    maxDistance = 50,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=50",
                        "https://i.pravatar.cc/300?img=51"
                    )
                ),
                UserEntity(
                    id = 13,
                    email = "lea@example.com",
                    password = "password123",
                    firstname = "Léa",
                    lastname = "Fontaine",
                    age = 24,
                    gender = "female",
                    bio = "Illustratrice freelance, fan de BD",
                    city = "Angers",
                    country = "France",
                    latitude = 47.4784,
                    longitude = -0.5632,
                    maxDistance = 40,
                    preferredGender = "all",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=55",
                        "https://i.pravatar.cc/300?img=56"
                    )
                ),
                UserEntity(
                    id = 14,
                    email = "maxime@example.com",
                    password = "password123",
                    firstname = "Maxime",
                    lastname = "Chevalier",
                    age = 28,
                    gender = "male",
                    bio = "Personal trainer, adepte du crossfit",
                    city = "Dijon",
                    country = "France",
                    latitude = 47.3220,
                    longitude = 5.0415,
                    maxDistance = 60,
                    preferredGender = "female",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=60",
                        "https://i.pravatar.cc/300?img=61"
                    )
                ),
                UserEntity(
                    id = 15,
                    email = "camille@example.com",
                    password = "password123",
                    firstname = "Camille",
                    lastname = "Girard",
                    age = 25,
                    gender = "other",
                    bio = "Artiste digital, passionné·e de mode et d'art contemporain",
                    city = "Paris",
                    country = "France",
                    latitude = 48.8566,
                    longitude = 2.3522,
                    maxDistance = 45,
                    preferredGender = "all",
                    photos = listOf(
                        "https://i.pravatar.cc/300?img=65",
                        "https://i.pravatar.cc/300?img=66"
                    )
                )
            )

            userDao.insertAll(demoUsers)

            Log.d("DatabaseSeeder", "Successfully seeded ${demoUsers.size} demo users")

        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "Error seeding database", e)
        }
    }
}

