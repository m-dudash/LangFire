package com.example.langfire_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.langfire_app.presentation.ui.theme.FireOrange
import com.example.langfire_app.presentation.ui.theme.FireOrangeDeep
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable

@Composable
fun SessionScreen() {
    var isStarted by remember { mutableStateOf(false) }
    if (!isStarted) SessionIntro(onStart = { isStarted = true }) else SessionFlashcard()
}

@Composable
fun SessionIntro(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(FireOrangeDeep, FireOrange))).statusBarsPadding(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("🔥", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Burn Session", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
            Text("10 words to master today.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = CircleShape, modifier = Modifier.height(60.dp).fillMaxWidth(0.7f)) {
                Text("LET'S BURN IT!", color = FireOrangeDeep, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun SessionFlashcard() {
    var flipped by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(progress = 0.3f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = FireOrange)
        Spacer(modifier = Modifier.weight(1f))
        ElevatedCard(modifier = Modifier.fillMaxWidth().aspectRatio(0.8f), shape = RoundedCornerShape(32.dp), onClick = { flipped = !flipped }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!flipped) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Huis", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(onClick = {}) { Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = FireOrange) }
                    }
                } else {
                    Text(text = "House", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold, color = FireOrange)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LargeActionButton(icon = Icons.Default.Close, color = Color(0xFFE53935), label = "Forgot")
            LargeActionButton(icon = Icons.Default.Done, color = Color(0xFF43A047), label = "Know it")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LargeActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(onClick = {}, modifier = Modifier.size(72.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = color.copy(alpha = 0.1f))) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}



