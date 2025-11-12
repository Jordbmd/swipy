package com.example.swipy.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit


object ThemePreferences {
    
    private const val PREFS_NAME = "swipy_theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    private const val KEY_USE_SYSTEM_THEME = "use_system_theme"
    
    private lateinit var prefs: SharedPreferences
    
    val isDarkMode = mutableStateOf(false)
    val useSystemTheme = mutableStateOf(true)
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Charger les préférences sauvegardées
        useSystemTheme.value = prefs.getBoolean(KEY_USE_SYSTEM_THEME, true)
        isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit {
            putBoolean(KEY_DARK_MODE, enabled)
            putBoolean(KEY_USE_SYSTEM_THEME, false)  // Désactive le thème système
        }
    }
    
    fun setUseSystemTheme(enabled: Boolean) {
        useSystemTheme.value = enabled
        prefs.edit {
            putBoolean(KEY_USE_SYSTEM_THEME, enabled)
        }
    }
    
    fun getDarkMode(): Boolean {
        return isDarkMode.value
    }
    
    fun isUsingSystemTheme(): Boolean {
        return useSystemTheme.value
    }
}
