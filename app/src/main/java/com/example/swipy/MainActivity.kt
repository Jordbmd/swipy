package com.example.swipy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.swipy.ui.HomeScreen
import com.example.swipy.viewModels.AuthViewModel
import com.example.swipy.ui.LoginScreen
import com.example.swipy.ui.RegisterScreen
import com.example.swipy.ui.LandingScreen
import com.example.swipy.models.User
import com.example.swipy.repositories.LocalAuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = AuthViewModel(LocalAuthRepository(applicationContext))

        setContent {
            MaterialTheme {
                var showLanding by remember { mutableStateOf(true) }
                var showRegister by remember { mutableStateOf(false) }
                var user by remember { mutableStateOf<User?>(null) }

                when {
                    user != null -> {
                        HomeScreen(
                            user = user!!,
                            onLogoutClick = {
                                vm.logout()
                                user = null
                                showLanding = true
                                showRegister = false
                            },
                            onBrowseClick = { /* TODO: future BrowseScreen */ }
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
