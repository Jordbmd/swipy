package com.example.swipy.data.local.datasource

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeedManager {

    private var isInitialized = false

    suspend fun initialize(context: Context, forceReseed: Boolean = false) =
        withContext(Dispatchers.IO) {
            if (isInitialized && !forceReseed) {
                Log.d("SeedManager", "Database already initialized")
                return@withContext
            }

            try {
                Log.d("SeedManager", "Initializing database seed")
                val seeder = DatabaseSeeder(context)
                seeder.seedDatabase(forceReseed)
                isInitialized = true
            } catch (e: Exception) {
                Log.e("SeedManager", "Error initializing database", e)
            }
        }
}