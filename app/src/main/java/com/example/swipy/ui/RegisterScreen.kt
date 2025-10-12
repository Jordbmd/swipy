package com.example.swipy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.swipy.models.RegisterData
import com.example.swipy.models.User
import com.example.swipy.viewModels.AuthViewModel

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onGoLogin: () -> Unit,
    onRegistered: (User) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.loggedInUser) {
        val user = state.loggedInUser
        if (user != null) {
            onRegistered(user)
        }
    }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("Inscription", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(lastname, { lastname = it }, label = { Text("lastname") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(firstname, { firstname = it }, label = { Text("firstname") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            password, { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            confirm, { confirm = it },
            label = { Text("Confirmer le mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.register(RegisterData(email, password, firstname, lastname, confirm)) }, enabled = !state.isLoading) {
            Text(
                if (state.isLoading) {
                    "…"
                } else {
                    "Créer le compte"
                },
            )
        }
        TextButton(onClick = onGoLogin, enabled = !state.isLoading) { Text("J’ai déjà un compte") }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
