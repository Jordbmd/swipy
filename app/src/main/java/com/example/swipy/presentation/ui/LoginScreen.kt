package com.example.swipy.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.swipy.domain.models.Credentials
import com.example.swipy.presentation.viewModels.AuthViewModel
import com.example.swipy.domain.models.User

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoRegister: () -> Unit,
    onLoggedIn: (User) -> Unit
) {
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state.loggedInUser) {
        val user = state.loggedInUser
        if (user != null) {
            onLoggedIn(user)
        }

    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("Connexion", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            password, { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { authViewModel.login(Credentials(email, password)) }, enabled = !state.isLoading) {
            Text(if (state.isLoading) "…" else "Se connecter")
        }
        TextButton(onClick = onGoRegister, enabled = !state.isLoading) { Text("Créer un compte") }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
