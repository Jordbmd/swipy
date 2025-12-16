package com.example.swipy.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.swipy.domain.models.User
import kotlinx.coroutines.launch

@Composable
fun SwipeCard(
    user: User,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val animatableOffsetX = remember { Animatable(0f) }
    val animatableOffsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    var currentPhotoIndex by remember { mutableIntStateOf(0) }
    val photoCount = user.photos.size

    val swipeThreshold = 300f
    val rotation = (offsetX / 20f).coerceIn(-20f, 20f)

    val likeOpacity = (offsetX / swipeThreshold).coerceIn(0f, 1f)
    val nopeOpacity = (-offsetX / swipeThreshold).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(550.dp)
            .graphicsLayer(
                translationX = animatableOffsetX.value,
                translationY = animatableOffsetY.value,
                rotationZ = rotation
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX > swipeThreshold -> {
                                    animatableOffsetX.animateTo(
                                        targetValue = 1000f,
                                        animationSpec = tween(300)
                                    )
                                    onSwipeRight()
                                    animatableOffsetX.snapTo(0f)
                                    animatableOffsetY.snapTo(0f)
                                }
                                offsetX < -swipeThreshold -> {
                                    animatableOffsetX.animateTo(
                                        targetValue = -1000f,
                                        animationSpec = tween(300)
                                    )
                                    onSwipeLeft()
                                    animatableOffsetX.snapTo(0f)
                                    animatableOffsetY.snapTo(0f)
                                }
                                else -> {
                                    animatableOffsetX.animateTo(0f, animationSpec = tween(300))
                                    animatableOffsetY.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                            offsetX = 0f
                            offsetY = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            animatableOffsetX.snapTo(offsetX)
                            animatableOffsetY.snapTo(offsetY)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentPhotoIndex > 0) {
                                currentPhotoIndex--
                            }
                        }
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentPhotoIndex < photoCount - 1) {
                                currentPhotoIndex++
                            }
                        }
                )
            }
            
            if (user.photos.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(user.photos.getOrNull(currentPhotoIndex) ?: user.photos.first()),
                    contentDescription = "Photo ${currentPhotoIndex + 1} de ${user.firstname}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Pas de photo",
                        color = Color.Gray,
                        fontSize = 20.sp
                    )
                }
            }

            if (photoCount > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(photoCount) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (index == currentPhotoIndex) 
                                        Color.White 
                                    else 
                                        Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            if (likeOpacity > 0) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Green.copy(alpha = likeOpacity))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "J'AIME",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            if (nopeOpacity > 0) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = nopeOpacity))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "NOPE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = "${user.firstname}, ${user.age}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (!user.city.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${user.city}${if (!user.country.isNullOrBlank()) ", ${user.country}" else ""}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                if (!user.bio.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = user.bio,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        maxLines = 3
                    )
                }
            }
        }
    }
}
