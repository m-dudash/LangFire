package com.example.langfire_app.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.HomeViewModel
import kotlin.math.cos
import android.widget.Toast
import androidx.compose.foundation.background

// ─────────────────────────────────────────────────────────────────────────────
// HOME SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLibraryClick: () -> Unit = {},
    onBurnClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onFortuneClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val requireProfile: (() -> Unit) -> Unit = { action ->
        if (state.hasActiveProfile) {
            action()
        } else {
            Toast.makeText(context, "First, create a profile in the Profile section!", Toast.LENGTH_SHORT).show()
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LangFireBottomBar(
                onLibraryClick = {requireProfile(onLibraryClick)},
                onBurnClick = { requireProfile { viewModel.onBurnClick(); onBurnClick() } },
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBar(
                languageFlag = state.languageFlag,
                languageName = state.languageName,
                languageLevel = state.languageLevel,
                xp = state.xp,
                onLanguageClick = { requireProfile(onLanguageClick) }            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val learned = state.learnedWords
                    val practiced = state.learnedWordsGoal
                    val toLearn = 500
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. To Learn
                        SmallProgressRing(
                            count = toLearn,
                            label = "To Learn",
                            color = Color.Gray,
                            containerColor = Color.LightGray.copy(alpha=0.3f),
                            progress = 1f
                        )
                        
                        // 2. Practiced
                        SmallProgressRing(
                            count = practiced,
                            label = "Practiced",
                            color = Color(0xFFFF9800),
                            containerColor = Color(0xFFFFE0B2),
                            progress = if(toLearn > 0) practiced.toFloat() / toLearn else 0f
                        )
                        
                        // 3. Learned
                        SmallProgressRing(
                            count = learned,
                            label = "Learned",
                            color = Color(0xFFFF3D00),
                            containerColor = Color(0xFFFFCCBC),
                            progress = if(practiced > 0) learned.toFloat() / practiced else 0f
                        )
                    }
                }

                FortuneWheelBadge(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 8.dp),
                    onClick = { requireProfile(onFortuneClick) }
                )

                FlameWithStreak(
                    streakDays = state.streakDays,
                    onClick = { requireProfile { viewModel.onBurnClick(); onBurnClick() } }
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(
    languageFlag: String,
    languageName: String,
    languageLevel: String,
    xp: Int,
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Surface(
            onClick = onLanguageClick,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                // Level Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = getLevelGradient(languageLevel),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                topStart = 16.dp, bottomStart = 16.dp,
                                topEnd = 4.dp, bottomEnd = 4.dp 
                            )
                        )
                ) {
                    Text(
                        text = languageLevel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Flag and Name
                Column {
                    Text(
                        text = languageFlag,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = languageName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // XP Badge
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50), // Pill shape
            color = Color(0xFF263238),
            border = androidx.compose.foundation.BorderStroke(2.dp, Brush.horizontalGradient(
                listOf(Color(0xFFFFD740), Color(0xFFFFAB00))
            )),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD740),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = formatXp(xp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

private fun getLevelGradient(level: String): Brush {
    // Unique gradients for each CEFR level
    // A: Greenish (Beginner)
    // B: Blue/Cyan (Intermediate)
    // C: Purple/Gold (Advanced/Master)
    return when (level.uppercase()) {
        "A1" -> Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))) // Light Green -> Green
        "A2" -> Brush.verticalGradient(listOf(Color(0xFF26A69A), Color(0xFF00695C))) // Teal -> Dark Teal
        "B1" -> Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0))) // Blue -> Dark Blue
        "B2" -> Brush.verticalGradient(listOf(Color(0xFF7E57C2), Color(0xFF4527A0))) // Deep Purple
        "C1" -> Brush.verticalGradient(listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A))) // Purple -> Magenta
        "C2" -> Brush.linearGradient(listOf(Color(0xFFFFD740), Color(0xFFFF6F00)))   // Gold -> Orange (Mastery)
        else -> Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray))
    }
}

private fun formatXp(xp: Int): String = if (xp >= 1000) {
    val k = xp / 1000
    val r = (xp % 1000) / 100
    if (r == 0) "${k}K XP" else "${k}.${r}K XP"
} else "$xp XP"

// ─────────────────────────────────────────────────────────────────────────────
// FLAME  +  STREAK
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FlameWithStreak(streakDays: Int, onClick: () -> Unit = {}) {
    val isStreakBroken = streakDays == 0

    // If streak is valid, we animate. Otherwise, static.
    val tr = rememberInfiniteTransition(label = "flame")

    val sway1 by if(isStreakBroken) remember { mutableFloatStateOf(0f) } else tr.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(3200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "s1"
    )
    val sway2 by if(isStreakBroken) remember { mutableFloatStateOf(0f) } else tr.animateFloat(
        1f, -1f,
        infiniteRepeatable(tween(2600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "s2"
    )
    val breathe by if(isStreakBroken) remember { mutableFloatStateOf(1f) } else tr.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(340.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
    ) {
        // Dynamic Flame Canvas
        Canvas(
            modifier = Modifier
                .width(220.dp)
                .height(300.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            
            // ── Main Vector Flame (Base) ──────────────────────────────
            // Outer flame
            drawFlameLayer(
                cx = cx, h = h,
                heightRatio = breathe,
                widthRatio = 0.88f,
                swayPx = sway1 * w * 0.05f,
                gradient = if (isStreakBroken) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFBDBDBD), Color(0xFF757575), Color(0xFF424242)),
                        startY = 0f, endY = h
                    )
                } else {
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f  to Color(0xFFFE9502).copy(alpha = 0f),
                            0.05f to Color(0xFFFFA040).copy(alpha = 0.6f),
                            0.35f to Color(0xFFFF5722),
                            0.70f to Color(0xFFE64A19),
                            1f  to Color(0xFFBF360C)
                        ),
                        startY = 0f,
                        endY = h * breathe
                    )
                }
            )

            // Middle flame
            drawFlameLayer(
                cx = cx, h = h,
                heightRatio = breathe * 0.80f,
                widthRatio = 0.60f,
                swayPx = sway2 * w * 0.04f,
                gradient = if (isStreakBroken) {
                    Brush.verticalGradient(
                         colors = listOf(Color(0xFFE0E0E0).copy(alpha=0.5f), Color(0xFF9E9E9E)),
                         startY = 0f, endY = h * 0.8f
                    )
                } else {
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f    to Color(0xFFFFD740).copy(alpha = 0f),
                            0.08f to Color(0xFFFFB300).copy(alpha = 0.7f),
                            0.40f to Color(0xFFFE9502),
                            0.80f to Color(0xFFFF5722),
                            1f    to Color(0xFFE64A19)
                        ),
                        startY = 0f,
                        endY = h * breathe * 0.80f
                    )
                }
            )

            // Inner core
            drawFlameLayer(
                cx = cx, h = h,
                heightRatio = breathe * 0.54f,
                widthRatio = 0.30f,
                swayPx = (sway1 + sway2) * 0.5f * w * 0.025f,
                gradient = if (isStreakBroken) {
                     Brush.verticalGradient(
                         colors = listOf(Color(0xFFF5F5F5).copy(alpha=0.3f), Color(0xFFBDBDBD)),
                         startY = 0f, endY = h * 0.54f
                    )
                } else {
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f    to Color(0xFFFFFDE7).copy(alpha = 0f),
                            0.10f to Color(0xFFFFF9C4).copy(alpha = 0.85f),
                            0.45f to Color(0xFFFFD740),
                            0.80f to Color(0xFFFFB300),
                            1f    to Color(0xFFFE9502)
                        ),
                        startY = 0f,
                        endY = h * breathe * 0.54f
                    )
                }
            )
            
            // ── Warm base glow (only if alive) ────────────────────────
            if (!isStreakBroken) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFE9502).copy(alpha = 0.65f),
                            Color(0xFFFF8A50).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, h),
                        radius = w * 0.6f
                    ),
                    topLeft = Offset(w * 0.04f, h * 0.84f),
                    size = Size(w * 0.92f, h * 0.18f)
                )
            }
        }

        // Streak number
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = streakDays.toString(),
                fontSize = 96.sp, // Made bigger
                fontWeight = FontWeight.Black,
                color = if (isStreakBroken) Color.Gray else MaterialTheme.colorScheme.onBackground,
                lineHeight = 90.sp
            )
            Text(
                text = "day streak",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isStreakBroken) Color.Gray else MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Draws one vector flame layer.
 * Updated to have a rounded bottom (arc) instead of just being cut off.
 */
private fun DrawScope.drawFlameLayer(
    cx: Float,
    h: Float,
    heightRatio: Float,
    widthRatio: Float,
    swayPx: Float,
    gradient: Brush
) {
    val fH = h * heightRatio
    val fW = size.width * widthRatio
    
    // Bottom arc control
    val bottomY = h
    val bottomArcH = fH * 0.15f // Height of the rounded bottom

    val tipY  = h - fH + bottomArcH // Adjust tip to keep visual height similar
    val tipX  = cx + swayPx

    val path = Path()
    // Start at bottom-center (lowest point of the round bottom)
    path.moveTo(cx, bottomY + bottomArcH * 0.2f)

    // Left curve bottom-up
    // Arc from bottom center to left width edge
    path.cubicTo(
        cx - fW * 0.3f, bottomY + bottomArcH * 0.2f, 
        cx - fW / 2f, bottomY,
        cx - fW / 2f, bottomY - bottomArcH
    )

    // Left side up to tip
    path.cubicTo(
        cx - fW * 0.70f, bottomY - fH * 0.40f,
        tipX - fW * 0.14f, bottomY - fH * 0.85f,
        tipX, tipY
    )
    
    // Right side down
    path.cubicTo(
        tipX + fW * 0.14f, bottomY - fH * 0.85f,
        cx + fW * 0.70f, bottomY - fH * 0.40f,
        cx + fW / 2f, bottomY - bottomArcH
    )
    
    // Arc from right width edge to bottom center
    path.cubicTo(
        cx + fW / 2f, bottomY,
        cx + fW * 0.3f, bottomY + bottomArcH * 0.2f,
        cx, bottomY + bottomArcH * 0.2f
    )
    
    path.close()

    drawPath(path = path, brush = gradient)
}

// ─────────────────────────────────────────────────────────────────────────────
// FORTUNE WHEEL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FortuneWheelBadge(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tr = rememberInfiniteTransition(label = "wheel")
    val rotation by tr.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "spin"
    )

    val segColors = listOf(
        Color(0xFFFE9502), Color(0xFFE53935),
        Color(0xFF9E9E9E), Color(0xFFFAFAFA),
        Color(0xFFFE9502), Color(0xFFE53935),
        Color(0xFF9E9E9E), Color(0xFFFAFAFA),
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(72.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.minDimension / 2f
            val innerR = outerR * 0.19f
            val sweep  = 360f / segColors.size

            rotate(rotation, pivot = Offset(cx, cy)) {
                segColors.forEachIndexed { i, color ->
                    drawArc(color = color, startAngle = i * sweep, sweepAngle = sweep, useCenter = true)
                    val rad = Math.toRadians((i * sweep).toDouble())
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(cx, cy),
                        end = Offset(cx + (outerR * cos(rad)).toFloat(), cy + (outerR * kotlin.math.sin(rad)).toFloat()),
                        strokeWidth = 1.5f
                    )
                }
                // Rim
                drawCircle(color = Color(0xFF5D4037), radius = outerR, style = Stroke(3f))
                // Rim dots
                repeat(segColors.size) { i ->
                    val rad = Math.toRadians((i * sweep + sweep / 2.0))
                    val r = outerR * 0.85f
                    drawCircle(
                        Color.White, radius = 3.5f,
                        center = Offset(cx + (r * cos(rad)).toFloat(), cy + (r * kotlin.math.sin(rad)).toFloat())
                    )
                }
                // Hub
                drawCircle(Color(0xFF212121), radius = innerR)
                drawCircle(Color(0xFFFE9502), radius = innerR, style = Stroke(2f))
            }
            // Fixed pointer
            val ptr = Path().apply {
                moveTo(cx - 7f, 1f); lineTo(cx + 7f, 1f); lineTo(cx, 13f); close()
            }
            drawPath(ptr, Color(0xFFFFD740))
            drawPath(ptr, Color(0xFF5D4037), style = Stroke(1.2f))
        }
        Spacer(Modifier.height(4.dp))
        Text("Daily", style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SMALL PROGRESS RING
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SmallProgressRing(
    count: Int,
    label: String,
    color: Color,
    containerColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val size = 72.dp // Increased size
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 6.dp.toPx() // Thicker stroke
                val radius = this.size.minDimension / 2f - stroke / 2f
                val cx = this.size.width / 2f; val cy = this.size.height / 2f
                
                // Track
                drawCircle(containerColor, radius = radius, style = Stroke(stroke))
                
                // Progress
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp, // Larger font
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM NAV
// ─────────────────────────────────────────────────────────────────────────────
internal enum class NavTab { LIBRARY, BURN, PROFILE }

@Composable
internal fun LangFireBottomBar(
    onLibraryClick: () -> Unit,
    onBurnClick: () -> Unit,
    onProfileClick: () -> Unit,
    initialSelected: NavTab = NavTab.BURN,
) {
    var selected by remember { mutableStateOf(initialSelected) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background Bar
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarItem(
                selected = selected == NavTab.LIBRARY,
                onClick = { selected = NavTab.LIBRARY; onLibraryClick() },
                icon = { Icon(Icons.Outlined.LibraryBooks, "Library") },
                label = { Text("Library") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor    = MaterialTheme.colorScheme.primaryContainer
                )
            )

            // Empty item to space out for the big button
            NavigationBarItem(
                selected = false,
                onClick = { },
                icon = { Box(Modifier.size(24.dp)) },
                enabled = false
            )

            NavigationBarItem(
                selected = selected == NavTab.PROFILE,
                onClick = { selected = NavTab.PROFILE; onProfileClick() },
                icon = { Icon(Icons.Outlined.Person, "Profile") },
                label = { Text("Profile") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor    = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        //  BURN BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(86.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        selected = NavTab.BURN
                        onBurnClick()
                    })
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5722).copy(alpha = 0.5f),
                            Color(0xFFFF8A50).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
            }
            
            // The Button Surface
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (selected == NavTab.BURN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp) // Inset for glow
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    // White tint for the icon inside the colored button
                    Icon(
                        imageVector = Icons.Outlined.Whatshot,
                        contentDescription = "Burn",
                        modifier = Modifier.size(42.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    com.example.langfire_app.presentation.ui.theme.LangFireappTheme { HomeScreen() }
}
