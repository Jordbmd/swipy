package com.example.swipy.data.local.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.filterDataStore by preferencesDataStore(name = "filter_preferences")

class FilterPreferences(private val context: Context) {
    private val MIN_AGE = intPreferencesKey("min_age")
    private val MAX_AGE = intPreferencesKey("max_age")
    private val MAX_DISTANCE = floatPreferencesKey("max_distance")
    private val PREFERRED_GENDER = stringPreferencesKey("preferred_gender")

    val minAge: Flow<Int> = context.filterDataStore.data.map { it[MIN_AGE] ?: 18 }
    val maxAge: Flow<Int> = context.filterDataStore.data.map { it[MAX_AGE] ?: 99 }
    val maxDistance: Flow<Float> = context.filterDataStore.data.map { it[MAX_DISTANCE] ?: 10000f }
    val preferredGender: Flow<String?> = context.filterDataStore.data.map { it[PREFERRED_GENDER] }

    suspend fun updateMinAge(age: Int) {
        context.filterDataStore.edit { it[MIN_AGE] = age }
    }

    suspend fun updateMaxAge(age: Int) {
        context.filterDataStore.edit { it[MAX_AGE] = age }
    }

    suspend fun updateMaxDistance(distance: Float) {
        context.filterDataStore.edit { it[MAX_DISTANCE] = distance }
    }

    suspend fun updatePreferredGender(gender: String?) {
        context.filterDataStore.edit { 
            if (gender != null) {
                it[PREFERRED_GENDER] = gender
            } else {
                it.remove(PREFERRED_GENDER)
            }
        }
    }
}