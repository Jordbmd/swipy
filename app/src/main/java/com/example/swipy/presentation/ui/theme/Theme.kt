package com.example.swipy.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPinkDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryPinkDarkVariant,
    onPrimaryContainer = OnPrimaryDark,
    
    secondary = SecondaryPurpleDark,
    onSecondary = OnPrimaryDark,
    
    tertiary = TertiaryOrangeDark,
    onTertiary = OnPrimaryDark,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    
    error = ErrorDark,
    onError = OnPrimaryDark
)


private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryPinkVariant,
    onPrimaryContainer = OnBackgroundLight,
    
    secondary = SecondaryPurple,
    onSecondary = OnPrimaryLight,
    
    tertiary = TertiaryOrange,
    onTertiary = OnPrimaryLight,
    
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    
    error = ErrorLight,
    onError = OnPrimaryLight
)


@Composable
fun SwipyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}