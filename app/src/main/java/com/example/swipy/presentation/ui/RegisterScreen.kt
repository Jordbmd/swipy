package com.example.swipy.presentation.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.swipy.domain.models.RegisterData
import com.example.swipy.domain.models.User
import com.example.swipy.presentation.viewModels.AuthViewModel

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onGoLogin: () -> Unit,
    onRegistered: (User) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.loggedInUser) {
        val user = state.loggedInUser
        if (user != null) onRegistered(user)
    }

    var step by remember { mutableIntStateOf(0) }

    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("autre") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> photoUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Étape ${step + 1} / 4",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        when (step) {
            0 -> {
                Text("Informations personnelles", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(firstname, { firstname = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(lastname, { lastname = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(age, { age = it.filter { c -> c.isDigit() } }, label = { Text("Âge") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        label = { Text("Genre") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("homme", "femme", "autre").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    expanded = false
                                }
                            )
                        }
                    }
                    Spacer(
                        Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                }
            }

            1 -> {
                Text("Identifiants de connexion", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmer le mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            2 -> {
                Text("Profil", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(bio, { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(city, { city = it }, label = { Text("Ville") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(country, { country = it }, label = { Text("Pays") }, modifier = Modifier.fillMaxWidth())
            }

            3 -> {
                Text("Photo de profil (optionnelle)", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Photo de profil",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Aucune photo sélectionnée", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choisir une photo")
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        vm.register(
                            RegisterData(
                                email = email,
                                password = password,
                                firstname = firstname,
                                lastname = lastname,
                                confirm = confirm,
                                age = age.toIntOrNull() ?: 18,
                                gender = gender,
                                bio = bio,
                                city = city,
                                country = country,
                                photos = if (photoUri != null) listOf(photoUri.toString()) else emptyList()
                            )
                        )
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isLoading) "…" else "Créer le compte")
                }

            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (step > 0) step-- },
                enabled = step > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Précédent")
            }

            if (step < 3) {
                IconButton(onClick = { step++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Suivant")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onGoLogin, enabled = !state.isLoading) {
            Text("J’ai déjà un compte")
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
