package com.example.swipy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.swipy.presentation.ui.HomeScreen
import com.example.swipy.presentation.ui.MatchScreen
import com.example.swipy.presentation.ui.MessagesScreen
import com.example.swipy.presentation.ui.MatchesListScreen
import com.example.swipy.presentation.ui.FilterScreen
import com.example.swipy.presentation.viewModels.AuthViewModel
import com.example.swipy.presentation.ui.LoginScreen
import com.example.swipy.presentation.ui.RegisterScreen
import com.example.swipy.presentation.ui.LandingScreen
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.AuthRepositoryImpl
import com.example.swipy.data.repository.SwipeRepositoryImpl
import com.example.swipy.data.repository.ChatRepositoryImpl
import com.example.swipy.presentation.viewModels.SwipeViewModel
import com.example.swipy.presentation.viewModels.ProfileViewModel
import com.example.swipy.presentation.viewModels.ChatViewModel
import com.example.swipy.data.local.datasource.SeedManager
import com.example.swipy.data.local.datasource.ThemePreferences
import com.example.swipy.data.local.datasource.FilterPreferences
import com.example.swipy.presentation.ui.theme.SwipyTheme
import com.example.swipy.domain.utils.LocationManager
import kotlinx.coroutines.launch
import com.example.swipy.presentation.ui.ProfileScreen
import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepo = AuthRepositoryImpl(applicationContext)
        val authViewModel = AuthViewModel(authRepo)
        val chatRepo = ChatRepositoryImpl(applicationContext)
        val filterPreferences = FilterPreferences(applicationContext)
        
        val locationPrefs = applicationContext.getSharedPreferences("location_prefs", MODE_PRIVATE)

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
                var isCheckingSession by remember { mutableStateOf(true) }
                var showLanding by remember { mutableStateOf(true) }
                var showRegister by remember { mutableStateOf(false) }
                var showProfile by remember { mutableStateOf(false) }
                var user by remember { mutableStateOf<User?>(null) }
                
                LaunchedEffect(Unit) {
                    val savedUser = authRepo.currentUserOrNull()
                    if (savedUser != null) {
                        user = savedUser
                        showLanding = false
                    }
                    isCheckingSession = false
                }
                var showMatchScreen by remember { mutableStateOf(false) }
                var showMessagesScreen by remember { mutableStateOf(false) }
                var showMatchesListScreen by remember { mutableStateOf(false) }
                var matchedUser by remember { mutableStateOf<User?>(null) }
                var isOfflineMode by remember { mutableStateOf(false) }
                var showLocationPermissionDialog by remember { mutableStateOf(false) }
                val hasAskedForLocationPermission = remember { 
                    locationPrefs.getBoolean("has_asked_location_permission", false) 
                }
                
                val locationManager = remember { LocationManager(applicationContext) }
                
                val locationPermissions = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                suspend fun updateUserLocation() {
                    if (user != null && locationManager.hasLocationPermission()) {
                        android.util.Log.d("MainActivity", "Updating location for user ${user!!.id}")
                        val result = locationManager.getCurrentLocation()
                        result.onSuccess { locationData ->
                            android.util.Log.d("MainActivity", "Location updated: ${locationData.city}, ${locationData.country}")
                            val updatedUser = authRepo.updateUserLocation(
                                userId = user!!.id,
                                city = locationData.city,
                                country = locationData.country,
                                latitude = locationData.latitude,
                                longitude = locationData.longitude
                            )
                            if (updatedUser != null) {
                                user = updatedUser
                            }
                        }.onFailure { error ->
                            android.util.Log.e("MainActivity", "Failed to update location: ${error.message}")
                        }
                    }
                }
                
                LaunchedEffect(locationPermissions.permissions.map { it.status.isGranted }) {
                    if (locationPermissions.permissions.any { it.status.isGranted }) {
                        updateUserLocation()
                    }
                }
                
                LaunchedEffect(user) {
                    if (user != null) {
                        isOfflineMode = authRepo.isOfflineMode()
                    
                        val hasPermission = locationManager.hasLocationPermission()
                        
                        if (hasPermission) {
                            updateUserLocation()
                        } else if (!hasAskedForLocationPermission) {
                            showLocationPermissionDialog = true
                            locationPrefs.edit().putBoolean("has_asked_location_permission", true).apply()
                        }
                    }
                }

                val swipeRepo = remember(user?.id) {
                    user?.let {
                        SwipeRepositoryImpl(applicationContext, it.id)
                    }
                }

                val swipeViewModel = remember(user?.id, swipeRepo) {
                    if (user != null && swipeRepo != null) {
                        android.util.Log.d("MainActivity", "Creating SwipeViewModel for user ${user!!.id}")
                        SwipeViewModel(
                            swipeRepository = swipeRepo,
                            filterPreferences = filterPreferences,
                            locationManager = locationManager,
                            currentUserId = user!!.id
                        )
                    } else {
                        null
                    }
                }

                val profileViewModel = remember {
                    ProfileViewModel(authRepo, applicationContext)
                }
                
                val profileState by profileViewModel.state.collectAsState()
                
                LaunchedEffect(profileState.user) {
                    if (profileState.user != null && user != null) {
                        if (profileState.user!!.id == user!!.id) {
                            user = profileState.user
                        }
                    }
                }

                val chatViewModel = remember(user?.id) {
                    user?.let { ChatViewModel(chatRepo, it.id) }
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
                    isCheckingSession -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    showMessagesScreen && user != null && matchedUser != null -> {
                        MessagesScreen(
                            matchedUser = matchedUser!!,
                            currentUserId = user!!.id,
                            chatViewModel = chatViewModel!!,
                            onBack = {
                                showMessagesScreen = false
                                showMatchScreen = false
                                swipeViewModel?.clearMatch()
                            }
                        )
                    }

                    showMatchesListScreen && user != null && swipeRepo != null -> {
                        MatchesListScreen(
                            currentUser = user!!,
                            swipeRepositoryImpl = swipeRepo!!,
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
                            swipeViewModel = swipeViewModel,
                            onBackClick = {
                                showProfile = false
                            },
                            onProfileUpdated = { updatedUser ->
                                user = updatedUser
                            },
                            onLogoutClick = {
                                authViewModel.logout()
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
                            authViewModel = authViewModel,
                            onGoLogin = { showRegister = false },
                            onRegistered = { u -> user = u }
                        )
                    }

                    else -> {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onGoRegister = { showRegister = true },
                            onLoggedIn = { u -> user = u }
                        )
                    }
                }
                
                if (showLocationPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showLocationPermissionDialog = false 
                        },
                        icon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = { 
                            Text("Activer la localisation ?") 
                        },
                        text = { 
                            Text(
                                "Swipy fonctionne mieux avec votre localisation activée.\n\n" +
                                "Cela nous permet de :\n" +
                                "• Trouver des personnes près de chez vous\n" +
                                "• Mettre à jour automatiquement votre position\n" +
                                "• Afficher des résultats pertinents\n\n" +
                                "Vous pouvez refuser et renseigner votre ville manuellement."
                            ) 
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLocationPermissionDialog = false
                                    locationPermissions.launchMultiplePermissionRequest()
                                }
                            ) {
                                Text("Activer")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { 
                                    showLocationPermissionDialog = false 
                                }
                            ) {
                                Text("Plus tard")
                            }
                        }
                    )
                }
            }
        }
    }
}
