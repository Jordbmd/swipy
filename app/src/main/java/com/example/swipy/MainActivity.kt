package com.example.swipy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.swipy.presentation.ui.HomeScreen
import com.example.swipy.presentation.ui.MatchScreen
import com.example.swipy.presentation.ui.MessagesScreen
import com.example.swipy.presentation.ui.MatchesListScreen
import com.example.swipy.presentation.viewModels.AuthViewModel
import com.example.swipy.presentation.ui.LoginScreen
import com.example.swipy.presentation.ui.RegisterScreen
import com.example.swipy.presentation.ui.LandingScreen
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.AuthRepositoryImpl
import com.example.swipy.data.repository.SwipeRepositoryImpl
import com.example.swipy.presentation.viewModels.SwipeViewModel
import com.example.swipy.presentation.viewModels.ProfileViewModel
import com.example.swipy.data.local.datasource.SeedManager
import com.example.swipy.data.local.datasource.ThemePreferences
import com.example.swipy.presentation.ui.theme.SwipyTheme
import kotlinx.coroutines.launch
import com.example.swipy.presentation.ui.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepo = AuthRepositoryImpl(applicationContext)
        val vm = AuthViewModel(authRepo)
        val userRepo = SwipeRepositoryImpl(applicationContext)
        
        ThemePreferences.init(applicationContext)
        
        lifecycleScope.launch {
            SeedManager.initialize(applicationContext)
        }

        setContent {
            val useSystemTheme by ThemePreferences.useSystemTheme
            val isDarkMode by ThemePreferences.isDarkMode
            val systemInDarkTheme = isSystemInDarkTheme()
            
            val darkTheme = if (useSystemTheme) systemInDarkTheme else isDarkMode
            
            SwipyTheme(darkTheme = darkTheme) {
                var showLanding by remember { mutableStateOf(true) }
                var showRegister by remember { mutableStateOf(false) }
                var showProfile by remember { mutableStateOf(false) }
                var user by remember { mutableStateOf<User?>(null) }
                var showMatchScreen by remember { mutableStateOf(false) }
                var showMessagesScreen by remember { mutableStateOf(false) }
                var showMatchesListScreen by remember { mutableStateOf(false) }
                var matchedUser by remember { mutableStateOf<User?>(null) }
                var isOfflineMode by remember { mutableStateOf(false) }
                
                LaunchedEffect(user) {
                    if (user != null) {
                        isOfflineMode = authRepo.isOfflineMode()
                    }
                }
                
                val swipeViewModel = remember(user?.id) {
                    user?.let { 
                        android.util.Log.d("MainActivity", "Creating SwipeViewModel for user ${it.id}")
                        SwipeViewModel(userRepo, it.id)
                    }
                }
                
                val profileViewModel = remember {
                    ProfileViewModel(authRepo)
                }
                
                LaunchedEffect(swipeViewModel) {
                    swipeViewModel?.state?.collect { state ->
                        if (state.matchedUser != null && !showMatchScreen) {
                            matchedUser = state.matchedUser
                            showMatchScreen = true
                        }
                    }
                }

                when {
                    showMessagesScreen && user != null && matchedUser != null -> {
                        MessagesScreen(
                            matchedUser = matchedUser!!,
                            onBack = {
                                showMessagesScreen = false
                                showMatchScreen = false
                                swipeViewModel?.clearMatch()
                            }
                        )
                    }
                    
                    showMatchesListScreen && user != null -> {
                        MatchesListScreen(
                            currentUser = user!!,
                            swipeRepositoryImpl = userRepo,
                            onBack = {
                                showMatchesListScreen = false
                            },
                            onMatchClick = { selectedMatch ->
                                matchedUser = selectedMatch
                                showMatchesListScreen = false
                                showMessagesScreen = true
                            }
                        )
                    }
                    
                    showMatchScreen && user != null && matchedUser != null -> {
                        MatchScreen(
                            currentUser = user!!,
                            matchedUser = matchedUser!!,
                            onSendMessage = {
                                showMatchScreen = false
                                showMessagesScreen = true
                            },
                            onKeepSwiping = {
                                showMatchScreen = false
                                swipeViewModel?.clearMatch()
                            }
                        )
                    }
                    showProfile && user != null -> {
                        ProfileScreen(
                            user = user!!,
                            profileViewModel = profileViewModel,
                            onBackClick = {
                                showProfile = false
                            },
                            onProfileUpdated = { updatedUser ->
                                user = updatedUser
                            },
                            onLogoutClick = {
                                vm.logout()
                                user = null
                                showProfile = false
                                showLanding = true
                                showRegister = false
                            }
                        )
                    }
                    
                    user != null && swipeViewModel != null -> {
                        HomeScreen(
                            swipeViewModel = swipeViewModel,

                            onMessagesClick = {
                                showMatchesListScreen = true
                            },
                            onProfileClick = {
                                showProfile = true
                            },
                            isOfflineMode = isOfflineMode
                        )
                    }

                    showLanding -> {
                        LandingScreen(
                            onLoginClick = {
                                showLanding = false
                                showRegister = false
                            },
                            onRegisterClick = {
                                showLanding = false
                                showRegister = true
                            }
                        )
                    }

                    showRegister -> {
                        RegisterScreen(
                            vm = vm,
                            onGoLogin = { showRegister = false },
                            onRegistered = { u -> user = u }
                        )
                    }

                    else -> {
                        LoginScreen(
                            vm = vm,
                            onGoRegister = { showRegister = true },
                            onLoggedIn = { u -> user = u }
                        )
                    }
                }
            }
        }
    }
}
