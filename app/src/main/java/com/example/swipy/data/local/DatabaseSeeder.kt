package com.example.swipy.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseSeeder(context: Context) {

    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "swipy-db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val userDao = db.userDao()

    suspend fun seedDatabase(forceReseed: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val userCount = userDao.getUserCount()
            if (userCount > 0 && !forceReseed) {
                Log.d("DatabaseSeeder", "Database already seeded with $userCount users")
                return@withContext
            }

            Log.d("DatabaseSeeder", "Starting database seeding...")
            
            val users = generateSeedUsers()
            userDao.insertAll(users)
            
            Log.d("DatabaseSeeder", "Database seeded successfully with ${users.size} users")
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "Error seeding database: ${e.message}", e)
        }
    }

    private fun generateSeedUsers(): List<UserEntity> {
        return listOf(
            UserEntity(
                email = "sophie.martin@example.com",
                password = "password123",
                firstname = "Sophie",
                lastname = "Martin",
                age = 25,
                gender = "femme",
                bio = "Passionnée de voyages et de photographie 📸 J'adore découvrir de nouveaux restaurants et partir à l'aventure !",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 30,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=1",
                    "https://i.pravatar.cc/300?img=5"
                )
            ),
            UserEntity(
                email = "lucas.dubois@example.com",
                password = "password123",
                firstname = "Lucas",
                lastname = "Dubois",
                age = 28,
                gender = "homme",
                bio = "Développeur passionné de tech 💻 Fan de sport et de musique électro. Toujours partant pour un bon resto !",
                city = "Lyon",
                country = "France",
                latitude = 45.7640,
                longitude = 4.8357,
                maxDistance = 50,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=12",
                    "https://i.pravatar.cc/300?img=13"
                )
            ),
            UserEntity(
                email = "emma.bernard@example.com",
                password = "password123",
                firstname = "Emma",
                lastname = "Bernard",
                age = 23,
                gender = "femme",
                bio = "Étudiante en art 🎨 Amoureuse de café et de lectures. Je cherche quelqu'un avec qui partager de beaux moments.",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 25,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=9",
                    "https://i.pravatar.cc/300?img=10"
                )
            ),
            UserEntity(
                email = "thomas.petit@example.com",
                password = "password123",
                firstname = "Thomas",
                lastname = "Petit",
                age = 30,
                gender = "homme",
                bio = "Chef cuisinier 👨‍🍳 Je crois que la meilleure façon de connaître quelqu'un est de partager un bon repas ensemble.",
                city = "Marseille",
                country = "France",
                latitude = 43.2965,
                longitude = 5.3698,
                maxDistance = 40,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=14",
                    "https://i.pravatar.cc/300?img=15"
                )
            ),
            UserEntity(
                email = "chloe.garcia@example.com",
                password = "password123",
                firstname = "Chloé",
                lastname = "Garcia",
                age = 26,
                gender = "femme",
                bio = "Infirmière dévouée ❤️ J'aime le yoga, la randonnée et les soirées cinéma. À la recherche de vraies connexions.",
                city = "Toulouse",
                country = "France",
                latitude = 43.6047,
                longitude = 1.4442,
                maxDistance = 35,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=20",
                    "https://i.pravatar.cc/300?img=23"
                )
            ),
            UserEntity(
                email = "maxime.rousseau@example.com",
                password = "password123",
                firstname = "Maxime",
                lastname = "Rousseau",
                age = 27,
                gender = "homme",
                bio = "Architecte passionné 📐 Fan de design et d'urbanisme. J'adore explorer de nouvelles villes et leur architecture.",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 20,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=17",
                    "https://i.pravatar.cc/300?img=18"
                )
            ),
            UserEntity(
                email = "lea.morel@example.com",
                password = "password123",
                firstname = "Léa",
                lastname = "Morel",
                age = 24,
                gender = "femme",
                bio = "Community manager créative 🌈 Toujours à la recherche de nouvelles inspirations. Amour des animaux et de la nature.",
                city = "Bordeaux",
                country = "France",
                latitude = 44.8378,
                longitude = -0.5792,
                maxDistance = 45,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=25",
                    "https://i.pravatar.cc/300?img=27"
                )
            ),
            UserEntity(
                email = "arthur.simon@example.com",
                password = "password123",
                firstname = "Arthur",
                lastname = "Simon",
                age = 29,
                gender = "homme",
                bio = "Ingénieur sportif 🏃‍♂️ Marathon runner et passionné de fitness. Cherche quelqu'un pour partager mes aventures.",
                city = "Nice",
                country = "France",
                latitude = 43.7102,
                longitude = 7.2620,
                maxDistance = 30,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=33",
                    "https://i.pravatar.cc/300?img=34"
                )
            ),
            UserEntity(
                email = "camille.laurent@example.com",
                password = "password123",
                firstname = "Camille",
                lastname = "Laurent",
                age = 25,
                gender = "femme",
                bio = "Professeure de yoga 🧘‍♀️ Zen attitude et bonne humeur. J'aime les couchers de soleil et les conversations profondes.",
                city = "Lyon",
                country = "France",
                latitude = 45.7640,
                longitude = 4.8357,
                maxDistance = 40,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=28",
                    "https://i.pravatar.cc/300?img=29"
                )
            ),
            UserEntity(
                email = "hugo.fournier@example.com",
                password = "password123",
                firstname = "Hugo",
                lastname = "Fournier",
                age = 31,
                gender = "homme",
                bio = "Photographe indépendant 📷 Je capture la beauté du monde un clic à la fois. Toujours en quête de nouvelles perspectives.",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 35,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=36",
                    "https://i.pravatar.cc/300?img=37"
                )
            ),
            UserEntity(
                email = "julie.girard@example.com",
                password = "password123",
                firstname = "Julie",
                lastname = "Girard",
                age = 27,
                gender = "femme",
                bio = "Journaliste curieuse 📝 Passionnée d'actualité et d'histoires humaines. J'aime débattre autour d'un verre.",
                city = "Nantes",
                country = "France",
                latitude = 47.2184,
                longitude = -1.5536,
                maxDistance = 50,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=31",
                    "https://i.pravatar.cc/300?img=32"
                )
            ),
            UserEntity(
                email = "nathan.mercier@example.com",
                password = "password123",
                firstname = "Nathan",
                lastname = "Mercier",
                age = 26,
                gender = "homme",
                bio = "Marketing manager créatif 🚀 Fan de startups et d'innovation. Toujours partant pour découvrir de nouveaux endroits.",
                city = "Lille",
                country = "France",
                latitude = 50.6292,
                longitude = 3.0573,
                maxDistance = 30,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=40",
                    "https://i.pravatar.cc/300?img=41"
                )
            ),
            UserEntity(
                email = "alice.blanc@example.com",
                password = "password123",
                firstname = "Alice",
                lastname = "Blanc",
                age = 24,
                gender = "femme",
                bio = "Designer UX/UI 🎨 Je crois au pouvoir du design pour changer le monde. Grande fan de café et de bonnes séries.",
                city = "Paris",
                country = "France",
                latitude = 48.8566,
                longitude = 2.3522,
                maxDistance = 25,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=35",
                    "https://i.pravatar.cc/300?img=38"
                )
            ),
            UserEntity(
                email = "paul.chevalier@example.com",
                password = "password123",
                firstname = "Paul",
                lastname = "Chevalier",
                age = 32,
                gender = "homme",
                bio = "Médecin généraliste 👨‍⚕️ À l'écoute et bienveillant. J'aime aider les autres et profiter de la vie simple.",
                city = "Strasbourg",
                country = "France",
                latitude = 48.5734,
                longitude = 7.7521,
                maxDistance = 40,
                preferredGender = "femme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=42",
                    "https://i.pravatar.cc/300?img=43"
                )
            ),
            UserEntity(
                email = "marine.roux@example.com",
                password = "password123",
                firstname = "Marine",
                lastname = "Roux",
                age = 28,
                gender = "femme",
                bio = "Avocate déterminée ⚖️ Je défends la justice avec passion. En dehors du tribunal, j'aime voyager et faire du shopping.",
                city = "Lyon",
                country = "France",
                latitude = 45.7640,
                longitude = 4.8357,
                maxDistance = 35,
                preferredGender = "homme",
                photos = listOf(
                    "https://i.pravatar.cc/300?img=44",
                    "https://i.pravatar.cc/300?img=45"
                )
            )
        )
    }
}

