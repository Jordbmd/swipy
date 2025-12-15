package com.example.swipy.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    minAge: Int,
    maxAge: Int,
    maxDistance: Float,
    onBackClick: () -> Unit,
    onApply: (Int, Int, Float) -> Unit
) {
    var currentMinAge by remember { mutableStateOf(minAge) }
    var currentMaxAge by remember { mutableStateOf(maxAge) }
    var currentMaxDistance by remember { mutableStateOf(maxDistance) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtres de recherche") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    "Âge",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${currentMinAge} ans")
                    Text("${currentMaxAge} ans")
                }
                RangeSlider(
                    value = currentMinAge.toFloat()..currentMaxAge.toFloat(),
                    onValueChange = { range ->
                        currentMinAge = range.start.toInt()
                        currentMaxAge = range.endInclusive.toInt()
                    },
                    valueRange = 18f..99f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text(
                    "Distance maximale",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${currentMaxDistance.toInt()} km")
                Slider(
                    value = currentMaxDistance,
                    onValueChange = { currentMaxDistance = it },
                    valueRange = 1f..200f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onApply(currentMinAge, currentMaxAge, currentMaxDistance)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Appliquer les filtres")
            }
        }
    }
}