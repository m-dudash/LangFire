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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FortuneScreen(
    viewModel: FortuneViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val isAlreadySpun by viewModel.isAlreadySpun.collectAsState()
    var isSpinning by remember { mutableStateOf(false) }
    var rewardPopup by remember { mutableStateOf<FortuneReward?>(null) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isAlreadySpun) {
        if (isAlreadySpun && !isSpinning && rewardPopup == null) {
            onDismiss()
        }
    }

    val segments = remember {
        val rewards = listOf(
            FortuneReward.Multiplier(2),
            FortuneReward.Multiplier(3),
            FortuneReward.Multiplier(5),
            FortuneReward.Xp(200),
            FortuneReward.Xp(300),
            FortuneReward.Xp(500),
            FortuneReward.UniqueAchievement
        )

        val fills = listOf(
            SegmentFill.Solid(Color(0xFFFF2400)),
            SegmentFill.Solid(Color(0xFFFFA500)),
            SegmentFill.Solid(Color(0xFFFFFFFF)),
            SegmentFill.Solid(Color(0xFFFF2400)),
            SegmentFill.Solid(Color(0xFFFFA500)),
            SegmentFill.Solid(Color(0xFFFFFFFF)),
            SegmentFill.Gradient(Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF651FFF))))
        )

        rewards.mapIndexed { index, reward ->
            FortuneSegment(
                reward = reward,
                label = generateLabel(reward),
                fill = fills[index % fills.size]
            )
        }
    }

    LaunchedEffect(rewardPopup) {
        if (rewardPopup != null) {
            delay(2500)
            rewardPopup = null
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
                        .pointerInput(Unit) {
                            detectTapGestures {
                                if (!isSpinning) {
                                    scope.launch {
                                        spinWheel(viewModel, rotation, segments) { reward ->
                                            isSpinning = false
                                            rewardPopup = reward
                                        }
                                    }
                                    isSpinning = true
                                }
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerR = (size.minDimension / 2f) - 24.dp.toPx()
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

                    val pointerPath = Path().apply {
                        moveTo(cx - 18.dp.toPx(), cy - outerR - 16.dp.toPx())
                        lineTo(cx + 18.dp.toPx(), cy - outerR - 16.dp.toPx())
                        lineTo(cx, cy - outerR + 18.dp.toPx())
                        close()
                    }
                    drawPath(pointerPath, color = Color(0xFFD50000))
                    drawPath(pointerPath, color = Color.White, style = Stroke(width = 3.dp.toPx()))
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = rewardPopup != null,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(200)) + scaleOut(tween(200))
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 10.dp,
                        shadowElevation = 12.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 WIN!", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFFFF007F))
                            Spacer(Modifier.height(12.dp))
                            Text(rewardText(rewardPopup), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isSpinning) {
                        scope.launch {
                            spinWheel(viewModel, rotation, segments) { reward ->
                                isSpinning = false
                                rewardPopup = reward
                            }
                        }
                        isSpinning = true
                    }
                },
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(0.8f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpinning) Color.Gray else Color(0xFFFF007F)
                ),
                enabled = !isSpinning
            ) {
                Text(
                    text = if (isSpinning) "SPINNING..." else "SPIN!",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private suspend fun spinWheel(
    viewModel: FortuneViewModel,
    rotation: Animatable<Float, *>,
    segments: List<FortuneSegment>,
    onFinished: (FortuneReward?) -> Unit
) {
    val reward = viewModel.spin()
    if (reward == null) {
        onFinished(null)
        return
    }

    val index = segments.indexOfFirst { sameReward(it.reward, reward) }.coerceAtLeast(0)
    val sweep = 360f / segments.size
    
    // Calculate the target angle so that the pointer (at -90 degrees) points to the center of the segment
    // Segment 'index' occupies [index * sweep, (index + 1) * sweep]
    // The current rotation puts segment 0 at [0, sweep]
    // To make the pointer (fixed at -90) point at segment 'index', we need:
    // (rotation + centerOfSegment) % 360 = 270 (which is same as -90)
    
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
        null -> ""
    }
}

private fun generateLabel(reward: FortuneReward): String {
    return when (reward) {
        is FortuneReward.Xp -> "+${reward.amount} XP"
        is FortuneReward.Multiplier -> "${reward.multiplier}x BOOST"
        FortuneReward.UniqueAchievement -> "SUPER WIN"
    }
}

private fun sameReward(a: FortuneReward, b: FortuneReward): Boolean {
    return when {
        a is FortuneReward.Xp && b is FortuneReward.Xp -> a.amount == b.amount
        a is FortuneReward.Multiplier && b is FortuneReward.Multiplier -> a.multiplier == b.multiplier
        a is FortuneReward.UniqueAchievement && b is FortuneReward.UniqueAchievement -> true
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