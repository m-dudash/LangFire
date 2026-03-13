package com.example.langfire_app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.langfire_app.domain.model.FortuneReward
import com.example.langfire_app.presentation.viewmodels.FortuneViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FortuneScreen(
    viewModel: FortuneViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    var isSpinning by remember { mutableStateOf(false) }
    // hasSpun stays true after spin completes so the wheel/button remain disabled
    var hasSpun by remember { mutableStateOf(false) }
    var rewardPopup by remember { mutableStateOf<FortuneReward?>(null) }
    var showPopup by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val availableRewards by viewModel.availableRewards.collectAsState()

    val segments = remember(availableRewards) {
        if (availableRewards.isEmpty()) {
            return@remember emptyList()
        }

        availableRewards.mapIndexed { index, reward ->
            val fill = when(reward) {
                FortuneReward.UniqueAchievement -> SegmentFill.Gradient(Brush.linearGradient(listOf(Color(0xFFFF00BF), Color(0xFF5219D0))))
                else -> {
                    // Regular pattern: index 0 (red), 1 (orange), 2 (white)
                    when (index % 3) {
                        0 -> SegmentFill.Solid(Color(0xFFFF2400))
                        1 -> SegmentFill.Solid(Color(0xFFFFA500))
                        else -> SegmentFill.Solid(Color(0xFFFFFFFF))
                    }
                }
            }
            FortuneSegment(
                reward = reward,
                label = generateLabel(reward),
                fill = fill
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("Fortune Wheel", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Spin the wheel and get bonuses!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                val textMeasurer = rememberTextMeasurer()

                Canvas(
                    modifier = Modifier
                        .size(340.dp)
                        .pointerInput(isSpinning, hasSpun) {
                            if (!isSpinning && !hasSpun) {
                                detectTapGestures {
                                    isSpinning = true
                                    scope.launch {
                                        spinWheel(viewModel, rotation, segments) { reward ->
                                            playRawSound(context, "win")
                                            rewardPopup = reward
                                            showPopup = true
                                            isSpinning = false
                                            hasSpun = true
                                            delay(2500)
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerR = (size.minDimension / 2f) - 24.dp.toPx()
                    if (segments.isNotEmpty()) {
                        val sweep = 360f / segments.size

                        rotate(rotation.value, pivot = Offset(cx, cy)) {
                            segments.forEachIndexed { i, seg ->
                                val start = i * sweep
                                when (val fill = seg.fill) {
                                    is SegmentFill.Solid -> drawArc(
                                        color = fill.color,
                                        startAngle = start,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        topLeft = Offset(cx - outerR, cy - outerR),
                                        size = Size(outerR * 2, outerR * 2)
                                    )
                                    is SegmentFill.Gradient -> drawArc(
                                        brush = fill.brush,
                                        startAngle = start,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        topLeft = Offset(cx - outerR, cy - outerR),
                                        size = Size(outerR * 2, outerR * 2)
                                    )
                                }
                            }

                        segments.forEachIndexed { i, seg ->
                            val startAngle = i * sweep
                            val centerAngle = startAngle + sweep / 2f

                            val radStart = Math.toRadians(startAngle.toDouble())
                            drawLine(
                                color = Color(0xFFFFD700),
                                start = Offset(cx, cy),
                                end = Offset(cx + outerR * cos(radStart).toFloat(), cy + outerR * sin(radStart).toFloat()),
                                strokeWidth = 3.dp.toPx()
                            )

                            rotate(centerAngle, pivot = Offset(cx, cy)) {
                                val isWhiteSegment = seg.fill is SegmentFill.Solid && seg.fill.color == Color(0xFFFFFFFF)
                                val textColor = if (isWhiteSegment) Color.Black else Color.White
                                val textShadow = if (isWhiteSegment) Color.Transparent else Color.Black.copy(alpha = 0.5f)

                                val textLayoutResult = textMeasurer.measure(
                                    text = seg.label,
                                    style = TextStyle(
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        shadow = Shadow(
                                            color = textShadow,
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f
                                        )
                                    )
                                )

                                val textX = cx + outerR * 0.35f
                                val textY = cy - textLayoutResult.size.height / 2f
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(textX, textY)
                                )
                            }
                        }

                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = outerR,
                            style = Stroke(width = 8.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFFF9C4),
                            radius = outerR - 4.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        segments.forEachIndexed { i, _ ->
                            val rad = Math.toRadians((i * sweep).toDouble())
                            val px = cx + outerR * cos(rad).toFloat()
                            val py = cy + outerR * sin(rad).toFloat()
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(px, py))
                            drawCircle(color = Color(0xFFBDBDBD), radius = 5.dp.toPx(), center = Offset(px, py), style = Stroke(1.dp.toPx()))
                        }

                        drawCircle(color = Color(0xFF2C2C2C), radius = outerR * 0.18f)
                        drawCircle(color = Color(0xFFFFD700), radius = outerR * 0.18f, style = Stroke(width = 4.dp.toPx()))
                        drawCircle(color = Color(0xFFFFC107), radius = outerR * 0.08f)
                    }
                    // Close the `if (segments.isNotEmpty())`
                    }

                    val pointerPath = Path().apply {
                        moveTo(cx - 18.dp.toPx(), cy - outerR - 16.dp.toPx())
                        lineTo(cx + 18.dp.toPx(), cy - outerR - 16.dp.toPx())
                        lineTo(cx, cy - outerR + 18.dp.toPx())
                        close()
                    }
                    drawPath(pointerPath, color = Color(0xFFD50000))
                    drawPath(pointerPath, color = Color.White, style = Stroke(width = 3.dp.toPx()))
                }

            }

            Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (!isSpinning && !hasSpun && segments.isNotEmpty()) {
                            isSpinning = true
                            scope.launch {
                                spinWheel(viewModel, rotation, segments) { reward ->
                                    playRawSound(context, "win")
                                    rewardPopup = reward
                                    showPopup = true
                                    isSpinning = false
                                    hasSpun = true
                                    delay(2500)
                                    onDismiss()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth(0.8f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            hasSpun -> Color(0xFF388E3C)  // green = done!
                            isSpinning || segments.isEmpty() -> Color.Gray
                            else -> Color(0xFFD77A0B)
                        }
                    ),
                    enabled = !isSpinning && !hasSpun && segments.isNotEmpty()
                ) {
                    Text(
                        text = when {
                            segments.isEmpty() -> "LOADING..."
                            hasSpun -> "DONE ✓"
                            isSpinning -> "SPINNING..."
                            else -> "SPIN!"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Win popup — floats centered, no dark backdrop ──────────────────
        AnimatedVisibility(
            visible = showPopup,
            enter = fadeIn(tween(400)) + scaleIn(tween(400, easing = FastOutSlowInEasing)),
            exit  = fadeOut(tween(250)) + scaleOut(tween(250))
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 16.dp,
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🎉 YOU WON!",
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = Color(0xFFFF007F)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (rewardPopup != null) rewardText(rewardPopup) else "Better luck next time!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private suspend fun spinWheel(
    viewModel: FortuneViewModel,
    rotation: Animatable<Float, *>,
    segments: List<FortuneSegment>,
    onFinished: suspend (FortuneReward?) -> Unit
) {
    // Always spin the wheel visually first, then resolve the reward.
    // A null reward (e.g. profile not yet loaded) falls back to a random segment
    // so the animation ALWAYS plays and the user is never left wondering why
    // nothing happened.
    val reward = viewModel.spin()

    val index = if (reward != null) {
        segments.indexOfFirst { sameReward(it.reward, reward) }.coerceAtLeast(0)
    } else {
        (segments.indices).random()
    }

    val sweep = 360f / segments.size

    // Calculate the target angle so that the pointer (at top / -90°) lands on
    // the centre of the winning segment.
    val centerAngleOfSegment = (index * sweep + sweep / 2f)
    val currentRotationMod = rotation.value % 360f
    val targetRotationDelta = 270f - (currentRotationMod + centerAngleOfSegment)

    val finalTargetRotation = rotation.value + (360f * 6) + targetRotationDelta

    rotation.animateTo(
        finalTargetRotation,
        animationSpec = tween(durationMillis = 4500, easing = FastOutSlowInEasing)
    )
    onFinished(reward)
}

private fun rewardText(reward: FortuneReward?): String {
    return when (reward) {
        is FortuneReward.Xp -> "+${reward.amount} XP"
        is FortuneReward.Multiplier -> "${reward.multiplier}x Boost (4h)"
        FortuneReward.UniqueAchievement -> "Unique Profile Design"
        FortuneReward.Freeze -> "Streak Freezer 🧊"
        null -> ""
    }
}

private fun generateLabel(reward: FortuneReward): String {
    return when (reward) {
        is FortuneReward.Xp -> "+${reward.amount} XP"
        is FortuneReward.Multiplier -> "${reward.multiplier}x BOOST"
        FortuneReward.UniqueAchievement -> "SUPER WIN"
        FortuneReward.Freeze -> "FREEZE 🧊"
    }
}

private fun sameReward(a: FortuneReward, b: FortuneReward): Boolean {
    return when {
        a is FortuneReward.Xp && b is FortuneReward.Xp -> a.amount == b.amount
        a is FortuneReward.Multiplier && b is FortuneReward.Multiplier -> a.multiplier == b.multiplier
        a is FortuneReward.UniqueAchievement && b is FortuneReward.UniqueAchievement -> true
        a is FortuneReward.Freeze && b is FortuneReward.Freeze -> true
        else -> false
    }
}

private data class FortuneSegment(
    val reward: FortuneReward,
    val label: String,
    val fill: SegmentFill
)

private sealed class SegmentFill {
    data class Solid(val color: Color) : SegmentFill()
    data class Gradient(val brush: Brush) : SegmentFill()
}