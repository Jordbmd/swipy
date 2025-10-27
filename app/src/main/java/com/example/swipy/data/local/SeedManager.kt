package com.example.swipy.data.local

import android.content.Context
import android.util.Log


object SeedManager {
    
    /**
     * @param context 
     * @param forceReseed 
     */
    suspend fun initialize(context: Context, forceReseed: Boolean = false) {
        val seeder = DatabaseSeeder(context)
        seeder.seedDatabase(forceReseed)
    }
}

