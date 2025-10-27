package com.example.swipy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.swipy.ui.HomeScreen
import com.example.swipy.ui.MatchScreen
import com.example.swipy.ui.MessagesScreen
import com.example.swipy.ui.MatchesListScreen
import com.example.swipy.viewModels.AuthViewModel
import com.example.swipy.ui.LoginScreen
import com.example.swipy.ui.RegisterScreen
import com.example.swipy.ui.LandingScreen
import com.example.swipy.models.User
import com.example.swipy.repositories.LocalAuthRepository
import com.example.swipy.repositories.UserRepository
import com.example.swipy.viewModels.SwipeViewModel
import com.example.swipy.viewModels.ProfileViewModel
import com.example.swipy.data.local.SeedManager
import kotlinx.coroutines.launch
import com.example.swipy.ui.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = AuthViewModel(LocalAuthRepository(applicationContext))
        val userRepo = UserRepository(applicationContext)
        
        lifecycleScope.launch {
            SeedManager.initialize(applicationContext)
        }

        setContent {
            MaterialTheme {
                var showLanding by remember { mutableStateOf(true) }
                var showRegister by remember { mutableStateOf(false) }
                var showProfile by remember { mutableStateOf(false) }
                var user by remember { mutableStateOf<User?>(null) }
                var showMatchScreen by remember { mutableStateOf(false) }
                var showMessagesScreen by remember { mutableStateOf(false) }
                var showMatchesListScreen by remember { mutableStateOf(false) }
                var matchedUser by remember { mutableStateOf<User?>(null) }
                
                val swipeViewModel = remember(user?.id) {
                    user?.let { 
                        android.util.Log.d("MainActivity", "Creating SwipeViewModel for user ${it.id}")
                        SwipeViewModel(userRepo, it.id)
                    }
                }
                
                val profileViewModel = remember {
                    ProfileViewModel(userRepo)
                }
                
                // Observer les matchs
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
                            currentUser = user!!,
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
                            userRepository = userRepo,
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
                            user = user!!,
                            swipeViewModel = swipeViewModel,
                            onLogoutClick = {
                                vm.logout()
                                user = null
                                showLanding = true
                                showRegister = false
                                showMatchScreen = false
                                showMessagesScreen = false
                                showMatchesListScreen = false
                                matchedUser = null
                            },
                            onMessagesClick = {
                                showMatchesListScreen = true
                            },
                            onProfileClick = {
                                showProfile = true
                            }
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
