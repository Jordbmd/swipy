package com.example.swipy.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
     Surface(modifier = Modifier.fillMaxSize()) {
         Column(
             modifier = Modifier
                 .fillMaxSize()
             .padding (24.dp),
         verticalArrangement = Arrangement.Center,
         horizontalAlignment = Alignment.CenterHorizontally
         ) {
         Text(
             text = "Swipy",
             fontSize = 48.sp,
             fontWeight = FontWeight.Bold,
             style = MaterialTheme.typography.headlineLarge
         )
         Spacer(modifier = Modifier.height(24.dp))
         Text(
             text = "Swipe. Match. Chat.",
             style = MaterialTheme.typography.bodyMedium
         )
         Spacer(modifier = Modifier.height(48.dp))
         Button(
             onClick = onLoginClick,
             modifier = Modifier.fillMaxWidth()
         ) {
             Text(text = "Se connecter")
         }
         Spacer(modifier = Modifier.height(12.dp))
         OutlinedButton(
             onClick = onRegisterClick,
             modifier = Modifier.fillMaxWidth()
         ) { Text("Créer un compte") }
         }
     }

}

