package com.example.swipy.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.swipy.domain.models.User
import com.example.swipy.presentation.viewModels.ProfileViewModel
import com.example.swipy.data.local.datasource.ThemePreferences
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    user: User,
    profileViewModel: ProfileViewModel,
    swipeViewModel: com.example.swipy.presentation.viewModels.SwipeViewModel? = null,
    onBackClick: () -> Unit,
    onProfileUpdated: (User) -> Unit,
    onLogoutClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    val profileState by profileViewModel.state.collectAsState()
    
    var firstname by remember { mutableStateOf(user.firstname) }
    var lastname by remember { mutableStateOf(user.lastname) }
    var age by remember { mutableStateOf(user.age.toString()) }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var city by remember { mutableStateOf(user.city ?: "") }
    var country by remember { mutableStateOf(user.country ?: "") }
    var latitude by remember { mutableStateOf(user.latitude) }
    var longitude by remember { mutableStateOf(user.longitude) }
    var maxDistance by remember { mutableStateOf(user.maxDistance.toString()) }
    
    val currentMinAge by swipeViewModel?.minAge?.collectAsState() ?: remember { mutableStateOf(18) }
    val currentMaxAge by swipeViewModel?.maxAge?.collectAsState() ?: remember { mutableStateOf(99) }
    val currentMaxDistance by swipeViewModel?.maxDistance?.collectAsState() ?: remember { mutableStateOf(10000f) }
    
    var minAge by remember { mutableStateOf(currentMinAge) }
    var maxAge by remember { mutableStateOf(currentMaxAge) }
    var photos by remember { mutableStateOf(user.photos.toList()) }
    
    LaunchedEffect(currentMinAge, currentMaxAge, currentMaxDistance) {
        minAge = currentMinAge
        maxAge = currentMaxAge
        maxDistance = currentMaxDistance.toInt().toString()
    }
    
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var newPhotoUrl by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var requestLocationAfterPermission by remember { mutableStateOf(false) }
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(locationPermissions.permissions.map { it.status.isGranted }) {
        if (requestLocationAfterPermission && locationPermissions.permissions.any { it.status.isGranted }) {
            requestLocationAfterPermission = false
            profileViewModel.getCurrentLocation { locationData, error ->
                if (locationData != null) {
                    city = locationData.city
                    country = locationData.country
                    latitude = locationData.latitude
                    longitude = locationData.longitude
                }
            }
        }
    }
    
    LaunchedEffect(profileState.user) {
        if (profileState.user != null) {
            val updatedUser = profileState.user!!
            firstname = updatedUser.firstname
            lastname = updatedUser.lastname
            age = updatedUser.age.toString()
            bio = updatedUser.bio ?: ""
            city = updatedUser.city ?: ""
            country = updatedUser.country ?: ""
            latitude = updatedUser.latitude
            longitude = updatedUser.longitude
            maxDistance = updatedUser.maxDistance.toString()
            photos = updatedUser.photos.toMutableList()
            onProfileUpdated(updatedUser)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            val ageInt = age.toIntOrNull()
                            val maxDistInt = maxDistance.toIntOrNull()
                            
                            if (ageInt != null && maxDistInt != null) {
                                if (city.isNotBlank() && country.isNotBlank()) {
                                    profileViewModel.validateLocation(city, country) { locationData, error ->
                                        if (locationData != null) {
                                            latitude = locationData.latitude
                                            longitude = locationData.longitude
                                        }
                                        
                                        profileViewModel.updateProfile(
                                            userId = user.id,
                                            firstname = firstname,
                                            lastname = lastname,
                                            age = ageInt,
                                            bio = bio,
                                            city = city,
                                            country = country,
                                            latitude = latitude,
                                            longitude = longitude,
                                            maxDistance = maxDistInt,
                                            photos = photos
                                        )
                                    }
                                } else {
                                    profileViewModel.updateProfile(
                                        userId = user.id,
                                        firstname = firstname,
                                        lastname = lastname,
                                        age = ageInt,
                                        bio = bio,
                                        city = city,
                                        country = country,
                                        latitude = latitude,
                                        longitude = longitude,
                                        maxDistance = maxDistInt,
                                        photos = photos
                                    )
                                }
                                
                                swipeViewModel?.updateFilters(
                                    minAge = minAge,
                                    maxAge = maxAge,
                                    maxDistance = maxDistInt.toFloat()
                                )
                                
                                isEditing = false
                            }
                        }) {
                            Text("Enregistrer")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = "${firstname.firstOrNull()?.uppercase() ?: ""}${lastname.firstOrNull()?.uppercase() ?: ""}"
                    Text(
                        text = initials,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ProfileInfoCard(
                    title = "Mes photos (${photos.size}/6)",
                    icon = Icons.Default.Face
                ) {
                    PhotoGrid(
                        photos = photos,
                        isEditing = isEditing,
                        onAddPhoto = { 
                            showAddPhotoDialog = true
                        },
                        onRemovePhoto = { photoUrl ->
                            photos = photos.filter { it != photoUrl }.toMutableList()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoCard(
                    title = "Informations personnelles",
                    icon = Icons.Default.Person
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = firstname,
                            onValueChange = { firstname = it },
                            label = { Text("Prénom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastname,
                            onValueChange = { lastname = it },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Âge") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileInfoRow(label = "Prénom", value = firstname)
                        ProfileInfoRow(label = "Nom", value = lastname)
                        ProfileInfoRow(label = "Âge", value = "$age ans")
                        ProfileInfoRow(label = "Genre", value = user.gender.capitalize())
                        ProfileInfoRow(label = "Email", value = user.email)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoCard(
                    title = "À propos de moi",
                    icon = Icons.Default.Info
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    } else {
                        Text(
                            text = bio.ifEmpty { "Aucune bio" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (bio.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoCard(
                    title = "Localisation",
                    icon = Icons.Default.LocationOn
                ) {
                    if (isEditing) {
                        Button(
                            onClick = {
                                if (locationPermissions.permissions.any { it.status.isGranted }) {
                                    profileViewModel.getCurrentLocation { locationData, error ->
                                        if (locationData != null) {
                                            city = locationData.city
                                            country = locationData.country
                                            latitude = locationData.latitude
                                            longitude = locationData.longitude
                                        }
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
                            )
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (profileState.isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Utiliser ma position GPS")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Saisie manuelle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = city,
                            onValueChange = { 
                                city = it
                                if (it.length >= 2) {
                                    profileViewModel.searchLocation("$it, $country")
                                }
                            },
                            label = { Text("Ville") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (city.isNotEmpty()) {
                                    IconButton(onClick = { city = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = country,
                            onValueChange = { 
                                country = it
                                if (city.length >= 2 && it.length >= 2) {
                                    profileViewModel.searchLocation("$city, $it")
                                }
                            },
                            label = { Text("Pays") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (country.isNotEmpty()) {
                                    IconButton(onClick = { country = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        )
                        
                        if (profileState.locationSuggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = "Suggestions :",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    profileState.locationSuggestions.take(5).forEach { suggestion ->
                                        TextButton(
                                            onClick = {
                                                city = suggestion.city
                                                country = suggestion.country
                                                latitude = suggestion.latitude
                                                longitude = suggestion.longitude
                                                profileViewModel.clearLocationSuggestions()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Start
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
                                        if (suggestion != profileState.locationSuggestions.last()) {
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ProfileInfoRow(label = "Ville", value = city.ifEmpty { "Non renseignée" })
                        ProfileInfoRow(label = "Pays", value = country.ifEmpty { "Non renseigné" })
                        if (latitude != null && longitude != null) {
                            ProfileInfoRow(
                                label = "Coordonnées", 
                                value = "%.4f, %.4f".format(latitude, longitude)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoCard(
                    title = "Préférences de recherche",
                    icon = Icons.Default.Settings
                ) {
                    if (isEditing) {
                        Text(
                            "Tranche d'âge",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${minAge} ans", style = MaterialTheme.typography.bodyMedium)
                            Text("${maxAge} ans", style = MaterialTheme.typography.bodyMedium)
                        }
                        RangeSlider(
                            value = minAge.toFloat()..maxAge.toFloat(),
                            onValueChange = { range ->
                                minAge = range.start.toInt()
                                maxAge = range.endInclusive.toInt()
                            },
                            valueRange = 18f..99f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Distance maximale",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${maxDistance} km", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = maxDistance,
                            onValueChange = { maxDistance = it },
                            label = { Text("Distance max (km)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileInfoRow(label = "Âge recherché", value = "$minAge - $maxAge ans")
                        ProfileInfoRow(label = "Distance maximale", value = "$maxDistance km")
                        ProfileInfoRow(
                            label = "Recherche", 
                            value = user.preferredGender?.capitalize() ?: "Tous"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoCard(
                    title = "Apparence",
                    icon = Icons.Default.Settings
                ) {
                    val useSystemTheme by ThemePreferences.useSystemTheme
                    val isDarkMode by ThemePreferences.isDarkMode
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thème système",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = useSystemTheme,
                            onCheckedChange = { enabled ->
                                ThemePreferences.setUseSystemTheme(enabled)
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode sombre",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (useSystemTheme) 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            if (useSystemTheme) {
                                Text(
                                    text = "Désactiver le thème système pour personnaliser",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled ->
                                ThemePreferences.setDarkMode(enabled)
                            },
                            enabled = !useSystemTheme
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Se déconnecter")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (profileState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            if (profileState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(profileState.successMessage!!)
                }
            }
        }
    }
    
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddPhotoDialog = false
                newPhotoUrl = ""
            },
            title = { Text("Ajouter une photo") },
            text = {
                Column {
                    Text("Entrez l'URL de la photo :")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhotoUrl,
                        onValueChange = { newPhotoUrl = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPhotoUrl.isNotEmpty() && photos.size < 6) {
                            photos = (photos + newPhotoUrl).toMutableList()
                        }
                        showAddPhotoDialog = false
                        newPhotoUrl = ""
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddPhotoDialog = false
                    newPhotoUrl = ""
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun PhotoGrid(
    photos: List<String>,
    isEditing: Boolean,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    Column {
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    ) {
                        if (index < photos.size) {
                            PhotoItem(
                                photoUrl = photos[index],
                                isEditing = isEditing,
                                onRemove = { onRemovePhoto(photos[index]) }
                            )
                        } else if (index == photos.size && photos.size < 6) {
                            AddPhotoButton(onClick = onAddPhoto, isEditing = isEditing)
                        } else {
                            EmptyPhotoSlot()
                        }
                    }
                }
            }
            if (row < 1) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PhotoItem(
    photoUrl: String,
    isEditing: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Photo",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        if (isEditing) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(Color.Red.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Supprimer",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddPhotoButton(onClick: () -> Unit, isEditing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isEditing) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = isEditing) { onClick() }
            .border(
                width = 2.dp,
                color = if (isEditing) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Ajouter",
            tint = if (isEditing) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun EmptyPhotoSlot() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    )
}

@Composable
fun ProfileInfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

