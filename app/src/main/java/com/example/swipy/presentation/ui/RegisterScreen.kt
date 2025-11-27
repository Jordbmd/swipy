package com.example.swipy.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.swipy.domain.models.RegisterData
import com.example.swipy.domain.models.User
import com.example.swipy.domain.utils.LocationManager
import com.example.swipy.presentation.viewModels.AuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onGoLogin: () -> Unit,
    onRegistered: (User) -> Unit
) {
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
    var validationError by remember { mutableStateOf<String?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var requestLocationAfterPermission by remember { mutableStateOf(false) }
    var locationSuggestions by remember { mutableStateOf<List<com.example.swipy.domain.utils.LocationData>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val locationManager = remember { LocationManager(context) }
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(locationPermissions.permissions.map { it.status.isGranted }) {
        if (requestLocationAfterPermission && locationPermissions.permissions.any { it.status.isGranted }) {
            requestLocationAfterPermission = false
            isLoadingLocation = true
            
            val result = locationManager.getCurrentLocation()
            result.onSuccess { locationData ->
                city = locationData.city
                country = locationData.country
            }
            
            isLoadingLocation = false
        }
    }

    fun validateCurrentStep(): String? {
        return when (step) {
            0 -> {
                when {
                    firstname.isBlank() -> "Le prénom est requis"
                    lastname.isBlank() -> "Le nom est requis"
                    age.isBlank() -> "L'âge est requis"
                    age.toIntOrNull() == null -> "L'âge doit être un nombre"
                    (age.toIntOrNull() ?: 0) < 18 -> "Vous devez avoir au moins 18 ans"
                    (age.toIntOrNull() ?: 0) > 120 -> "L'âge n'est pas valide"
                    else -> null
                }
            }
            1 -> {
                when {
                    city.isBlank() -> "La ville est requise"
                    country.isBlank() -> "Le pays est requis"
                    city.length < 2 -> "La ville doit contenir au moins 2 caractères"
                    country.length < 2 -> "Le pays doit contenir au moins 2 caractères"
                    else -> null
                }
            }
            2 -> {
                when {
                    email.isBlank() -> "L'email est requis"
                    !email.contains("@") -> "L'email n'est pas valide"
                    password.isBlank() -> "Le mot de passe est requis"
                    password.length < 6 -> "Le mot de passe doit contenir au moins 6 caractères"
                    confirm.isBlank() -> "Veuillez confirmer le mot de passe"
                    password != confirm -> "Les mots de passe ne correspondent pas"
                    else -> null
                }
            }
            3 -> {
                when {
                    bio.isBlank() -> "La bio est requise"
                    else -> null
                }
            }
            else -> null
        }
    }

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
                Text("Localisation", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (locationPermissions.permissions.any { it.status.isGranted }) {
                            scope.launch {
                                isLoadingLocation = true
                                val result = locationManager.getCurrentLocation()
                                result.onSuccess { locationData ->
                                    city = locationData.city
                                    country = locationData.country
                                }
                                isLoadingLocation = false
                            }
                        } else {
                            requestLocationAfterPermission = true
                            locationPermissions.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    enabled = !isLoadingLocation
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Récupération...")
                    } else {
                        Text("Utiliser ma position GPS")
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OU",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                
                Spacer(Modifier.height(12.dp))
                
                Column {
                    OutlinedTextField(
                        value = city, 
                        onValueChange = { newValue ->
                            city = newValue
                            if (newValue.length >= 2) {
                                showSuggestions = true
                                scope.launch {
                                    val query = if (country.isNotEmpty()) "$newValue, $country" else newValue
                                    val result = locationManager.searchLocation(query)
                                    result.onSuccess { suggestions ->
                                        locationSuggestions = suggestions
                                    }
                                }
                            } else {
                                showSuggestions = false
                                locationSuggestions = emptyList()
                            }
                        }, 
                        label = { Text("Ville") }, 
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (city.isNotEmpty()) {
                                IconButton(onClick = { 
                                    city = ""
                                    showSuggestions = false
                                    locationSuggestions = emptyList()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        }
                    )
                    
                    if (showSuggestions && locationSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                locationSuggestions.take(5).forEach { suggestion ->
                                    TextButton(
                                        onClick = {
                                            city = suggestion.city
                                            country = suggestion.country
                                            showSuggestions = false
                                            locationSuggestions = emptyList()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Place,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${suggestion.city}, ${suggestion.country}")
                                        }
                                    }
                                    if (suggestion != locationSuggestions.take(5).last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = country, 
                    onValueChange = { newValue ->
                        country = newValue
                        if (newValue.length >= 2 && city.isNotEmpty()) {
                            showSuggestions = true
                            scope.launch {
                                val query = "$city, $newValue"
                                val result = locationManager.searchLocation(query)
                                result.onSuccess { suggestions ->
                                    locationSuggestions = suggestions
                                }
                            }
                        }
                    }, 
                    label = { Text("Pays") }, 
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (country.isNotEmpty()) {
                            IconButton(onClick = { 
                                country = ""
                                showSuggestions = false
                                locationSuggestions = emptyList()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    }
                )
            }

            2 -> {
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

            3 -> {
                Text("Profil et photo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(bio, { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                
                Text("Photo de profil (optionnelle)", style = MaterialTheme.typography.titleMedium)
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
                        authViewModel.register(
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
                onClick = { 
                    if (step > 0) {
                        step--
                        validationError = null
                    }
                },
                enabled = step > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Précédent")
            }

            if (step < 3) {
                IconButton(onClick = { 
                    val error = validateCurrentStep()
                    if (error != null) {
                        validationError = error
                    } else {
                        validationError = null
                        step++
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Suivant")
                }
            }
        }

        validationError?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it, 
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onGoLogin, enabled = !state.isLoading) {
            Text("J’ai déjà un compte")
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
