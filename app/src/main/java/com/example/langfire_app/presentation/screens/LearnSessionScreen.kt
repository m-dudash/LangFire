package com.example.langfire_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.*

// ─── Root screen ─────────────────────────────────────────────────────────────

@Composable
fun SessionScreen(
    viewModel: LearnViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = state.sessionPhase,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        label = "SessionPhaseTransition"
    ) { phase ->
        when (phase) {
            SessionPhase.LOADING  -> SessionLoadingScreen()
            SessionPhase.INTRO    -> SessionIntroScreen(
                wordCount = state.totalInSession,
                onStart   = viewModel::startSession
            )
            SessionPhase.STUDYING -> SessionStudyScreen(
                state   = state,
                onReveal = viewModel::revealCard,
                onAnswer = viewModel::answer
            )
            SessionPhase.FINISHED -> SessionFinishedScreen(
                correctCount = state.correctCount,
                forgotCount  = state.forgotCount,
                total        = state.totalInSession,
                onRestart    = viewModel::restart
            )
            SessionPhase.EMPTY    -> SessionEmptyScreen()
        }
    }
}

// ─── Loading ─────────────────────────────────────────────────────────────────

@Composable
private fun SessionLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = FireOrange)
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun SessionEmptyScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🎉", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No words to review!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Go to Library and mark words to learn, then come back!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Intro ───────────────────────────────────────────────────────────────────

@Composable
private fun SessionIntroScreen(wordCount: Int, onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FireOrangeDeep, FireOrange)))
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated fire emoji
            val infiniteTransition = rememberInfiniteTransition(label = "fireAnim")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "firePulse"
            )

            Text("🔥", fontSize = 80.sp, modifier = Modifier.scale(scale))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Burn Session",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$wordCount words to master today.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Word count chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionInfoChip(label = "Cards", value = "$wordCount")
                SessionInfoChip(label = "Mode", value = "Flashcard")
            }

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Text(
                    "LET'S BURN IT!",
                    color = FireOrangeDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SessionInfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─── Study (main flashcard loop) ─────────────────────────────────────────────

@Composable
private fun SessionStudyScreen(
    state: LearnUiState,
    onReveal: () -> Unit,
    onAnswer: (SrsEngine.Quality) -> Unit
) {
    val word = state.currentWord ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar + counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(CircleShape),
                color = FireOrange,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${state.currentIndex + 1} / ${state.totalInSession}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Flashcard
        AnimatedContent(
            targetState = word.wordId,
            transitionSpec = {
                (slideInHorizontally { it / 2 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 2 } + fadeOut())
            },
            label = "CardTransition"
        ) {
            FlipCard(
                word        = word.word,
                translation = word.translation.ifBlank { "—" },
                isRevealed  = state.cardPhase == CardPhase.REVEALED,
                onClick     = onReveal
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons — only shown after flip
        AnimatedVisibility(
            visible = state.cardPhase == CardPhase.REVEALED,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "How well did you know it?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnswerButton(
                        icon     = Icons.Default.Close,
                        label    = "Forgot",
                        color    = Color(0xFFE53935),
                        onClick  = { onAnswer(SrsEngine.Quality.FORGOT) }
                    )
                    AnswerButton(
                        icon     = Icons.Default.Done,
                        label    = "Got it",
                        color    = EmeraldGreen,
                        onClick  = { onAnswer(SrsEngine.Quality.GOOD) }
                    )
                    AnswerButton(
                        icon     = Icons.Default.Star,
                        label    = "Easy!",
                        color    = GoldXP,
                        onClick  = { onAnswer(SrsEngine.Quality.EASY) }
                    )
                }
            }
        }

        // Hint text — shown before flip
        AnimatedVisibility(
            visible = state.cardPhase == CardPhase.FRONT,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Text(
                text = "Tap the card to reveal translation",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ─── Flashcard ───────────────────────────────────────────────────────────────

@Composable
private fun FlipCard(
    word: String,
    translation: String,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    val isFrontVisible = rotation <= 90f

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .graphicsLayer {
                rotationY       = rotation
                cameraDistance  = 12f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        shape     = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = if (isFrontVisible)
                MaterialTheme.colorScheme.surface
            else
                FireContainer
        )
    ) {
        Box(
            modifier          = Modifier.fillMaxSize(),
            contentAlignment  = Alignment.Center
        ) {
            if (isFrontVisible) {
                // ── Front side ──
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(32.dp)
                ) {
                    Text(
                        text       = word,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color      = MaterialTheme.colorScheme.onSurface,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = CircleShape,
                        color = FireOrange.copy(alpha = 0.1f)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Outlined.VolumeUp,
                                contentDescription = "Pronounce",
                                tint               = FireOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text  = "Tap to reveal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // ── Back side (mirrored to read correctly) ──
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier
                        .padding(32.dp)
                        .graphicsLayer { rotationY = 180f }
                ) {
                    Text(
                        text       = "Translation",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = OnFireContainer.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text       = translation,
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color      = OnFireContainer,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Original word reminder
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OnFireContainer.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text      = word,
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = OnFireContainer,
                            modifier  = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Answer button ───────────────────────────────────────────────────────────

@Composable
private fun AnswerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick  = onClick,
            modifier = Modifier.size(72.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = color.copy(alpha = 0.12f)
            )
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint               = color,
                modifier           = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

// ─── Finished ────────────────────────────────────────────────────────────────

@Composable
private fun SessionFinishedScreen(
    correctCount: Int,
    forgotCount: Int,
    total: Int,
    onRestart: () -> Unit
) {
    val accuracy = if (total > 0) (correctCount * 100f / total).toInt() else 0
    val emoji = when {
        accuracy >= 90 -> "🏆"
        accuracy >= 70 -> "🎉"
        accuracy >= 50 -> "💪"
        else           -> "🔥"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FireOrangeDeep, FireOrange)))
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text(emoji, fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Session Complete!",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color      = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Stats row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinishStat(label = "Correct",  value = "$correctCount", color = Color(0xFF81C784))
                FinishStat(label = "Forgot",   value = "$forgotCount",  color = Color(0xFFEF9A9A))
                FinishStat(label = "Accuracy", value = "$accuracy%",    color = Color.White)
            }

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onRestart,
                colors  = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape   = CircleShape,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Text(
                    "NEW SESSION",
                    color      = FireOrangeDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FinishStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color      = color
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}
