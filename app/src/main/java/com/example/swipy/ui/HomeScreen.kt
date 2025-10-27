package com.example.swipy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import com.example.swipy.models.User
import com.example.swipy.viewModels.SwipeViewModel
import com.example.swipy.ui.components.SwipeCard

@Composable
fun HomeScreen(
    user: User,
    swipeViewModel: SwipeViewModel,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val swipeState by swipeViewModel.state.collectAsState()
    val currentProfile = swipeViewModel.getCurrentProfile()

    // Debug
    LaunchedEffect(swipeState, currentProfile) {
        android.util.Log.d("HomeScreen", "isLoading=${swipeState.isLoading}, " +
                "profiles=${swipeState.profiles.size}, " +
                "currentIndex=${swipeState.currentProfileIndex}, " +
                "currentProfile=${currentProfile?.firstname ?: "null"}")
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = "swipy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    swipeState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Chargement des profils...")
                    }

                    currentProfile != null -> {
                        SwipeCard(
                            user = currentProfile,
                            onSwipeLeft = { swipeViewModel.swipeLeft() },
                            onSwipeRight = { swipeViewModel.swipeRight() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    swipeState.profiles.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 64.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Plus de profils disponibles",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Revenez plus tard pour découvrir de nouveaux profils !",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { swipeViewModel.loadProfiles() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Recharger")
                            }
                        }
                    }
                }
            }

            if (currentProfile != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { swipeViewModel.swipeLeft() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE57373))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Non",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { swipeViewModel.loadProfiles() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64B5F6))
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recharger",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { swipeViewModel.swipeRight() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF81C784))
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "J'aime",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
