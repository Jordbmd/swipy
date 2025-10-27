package com.example.swipy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swipy.models.User
import com.example.swipy.viewModels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    profileViewModel: ProfileViewModel,
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
    var maxDistance by remember { mutableStateOf(user.maxDistance.toString()) }
    var photos by remember { mutableStateOf(user.photos.toMutableList()) }
    
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var newPhotoUrl by remember { mutableStateOf("") }
    
    LaunchedEffect(profileState.user) {
        if (profileState.user != null) {
            val updatedUser = profileState.user!!
            firstname = updatedUser.firstname
            lastname = updatedUser.lastname
            age = updatedUser.age.toString()
            bio = updatedUser.bio ?: ""
            city = updatedUser.city ?: ""
            country = updatedUser.country ?: ""
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            val ageInt = age.toIntOrNull()
                            val maxDistInt = maxDistance.toIntOrNull()
                            
                            if (ageInt != null && maxDistInt != null) {
                                profileViewModel.updateProfile(
                                    userId = user.id,
                                    firstname = firstname,
                                    lastname = lastname,
                                    age = ageInt,
                                    bio = bio,
                                    city = city,
                                    country = country,
                                    maxDistance = maxDistInt,
                                    photos = photos
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
                // Photo de profil (juste les initiales pour l'instant)
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
                
                // Grille de photos
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
                
                // Informations du profil
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
                
                // Bio
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
                
                // Localisation
                ProfileInfoCard(
                    title = "Localisation",
                    icon = Icons.Default.LocationOn
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Ville") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Pays") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileInfoRow(label = "Ville", value = city.ifEmpty { "Non renseignée" })
                        ProfileInfoRow(label = "Pays", value = country.ifEmpty { "Non renseigné" })
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Préférences
                ProfileInfoCard(
                    title = "Préférences de recherche",
                    icon = Icons.Default.Settings
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = maxDistance,
                            onValueChange = { maxDistance = it },
                            label = { Text("Distance max (km)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileInfoRow(label = "Distance maximale", value = "$maxDistance km")
                        ProfileInfoRow(
                            label = "Recherche", 
                            value = user.preferredGender?.capitalize() ?: "Tous"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bouton de déconnexion
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
    
    // Dialog pour ajouter une photo
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
                            // Photo existante
                            PhotoItem(
                                photoUrl = photos[index],
                                isEditing = isEditing,
                                onRemove = { onRemovePhoto(photos[index]) }
                            )
                        } else if (index == photos.size && photos.size < 6) {
                            // Bouton ajouter
                            AddPhotoButton(onClick = onAddPhoto, isEditing = isEditing)
                        } else {
                            // Case vide
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

