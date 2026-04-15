package com.example.langfire_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.media.RingtoneManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.langfire_app.domain.model.SessionWord
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.*
import com.example.langfire_app.domain.model.EngineResult
import com.example.langfire_app.domain.model.Achievement
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler


@Composable
fun SessionScreen(
    viewModel: LearnViewModel = hiltViewModel(),
    onFinishClick: (Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showExitWarning by remember { mutableStateOf(false) }

    BackHandler(enabled = state.sessionPhase == SessionPhase.STUDYING) {
        showExitWarning = true
    }

    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            title = {
                Text(
                    text = "Exit Session?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "If you leave now, you'll lose all your progress in this session.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitWarning = false
                        onFinishClick(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Exit", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitWarning = false }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    AnimatedContent(
        targetState = state.sessionPhase,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
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
                state     = state,
                viewModel = viewModel
            )
            SessionPhase.FINISHED -> SessionFinishedScreen(
                correctCount = state.correctCount,
                forgotCount  = state.forgotCount,
                total        = state.totalInSession,
                engineResult = state.engineResult,
                onFinishClick = onFinishClick
            )
            SessionPhase.EMPTY    -> SessionEmptyScreen()
        }
    }
}


@Composable
private fun SessionLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = FireOrange, strokeWidth = 5.dp, modifier = Modifier.size(64.dp))
    }
}

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
            Text("🎉", fontSize = 96.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Not Enough Words!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You need at least 15 words to start a session. Go to the Library to add more words to your learning queue.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}


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
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "fireAnim")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "firePulse"
            )

            Text("🔥", fontSize = 100.sp, modifier = Modifier.scale(scale))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Burn Session",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    "$wordCount EXERCISES TODAY",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    letterSpacing = 1.5.sp
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(0.85f)
                    .shadow(16.dp, CircleShape, spotColor = Color.White.copy(alpha = 0.5f))
            ) {
                Text(
                    "LET'S BURN IT!",
                    color = FireOrangeDeep,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


@Composable
private fun SessionStudyScreen(
    state: LearnUiState,
    viewModel: LearnViewModel
) {
    val exercise = state.currentExercise ?: return
    val context = LocalContext.current

    LaunchedEffect(state.cardPhase) {
        if (state.cardPhase == CardPhase.REVEALED) {
            val ex = state.currentExercise
            if (ex != null) {
                if (state.isTypeCorrect != null) {
                    playRawSound(context, if (state.isTypeCorrect == true) "correct" else "incorrect")
                } else if (state.selectedWordId != null) {
                    playRawSound(context, if (state.selectedWordId == ex.word.wordId) "correct" else "incorrect")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { viewModel.skipDelay() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(FireOrange.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, FireOrange.copy(alpha = 0.25f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(CircleShape),
                color = FireOrange,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${state.currentIndex + 1} / ${state.totalInSession}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        StreakProgressRing(
            correctToday = state.correctToday,
            dailyGoal = state.dailyGoal
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = exercise,
                transitionSpec = {
                    (slideInHorizontally { it / 2 } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally { -it / 2 } + fadeOut(tween(300)))
                },
                label = "ExerciseTransition"
            ) { currentExercise ->
                when (currentExercise.type) {
                    ExerciseType.FLASHCARD_L1 -> FlashcardExercise(state, viewModel, currentExercise, showNativeFirst = true)
                    ExerciseType.FLASHCARD_L2 -> FlashcardExercise(state, viewModel, currentExercise, showNativeFirst = false)
                    ExerciseType.LISTENING_FLIP -> ListeningFlipExercise(state, viewModel, currentExercise)
                    ExerciseType.TRANSLATE_L1 -> MultipleChoiceExercise(state, viewModel, currentExercise, showWord = true)
                    ExerciseType.TRANSLATE_L2 -> MultipleChoiceExercise(state, viewModel, currentExercise, showWord = false)
                    ExerciseType.LISTENING_CHOOSE -> ListeningChoiceExercise(state, viewModel, currentExercise)
                    ExerciseType.LISTENING_TYPE -> ListeningTypeExercise(state, viewModel, currentExercise)
                    ExerciseType.MATCHING -> MatchingExercise(state, viewModel, currentExercise)
                }
            }
        }
        
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun FlashcardExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem, showNativeFirst: Boolean) {
    val context = LocalContext.current

    LaunchedEffect(exercise.word.wordId) {
        if (!showNativeFirst) {
            playWordAudio(context, exercise.word.audioPath)
        }
    }

    LaunchedEffect(state.cardPhase) {
        if (state.cardPhase == CardPhase.REVEALED && showNativeFirst) {
            playWordAudio(context, exercise.word.audioPath)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FlipCard(
            item        = exercise.word,
            isRevealed  = state.cardPhase == CardPhase.REVEALED,
            blurContent = false,
            audioOnlyFront = false,
            showNativeFirst = showNativeFirst,
            onClick     = viewModel::revealCard,
            onPlayAudio = { playWordAudio(context, exercise.word.audioPath) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = state.cardPhase == CardPhase.REVEALED,
                enter   = fadeIn() + scaleIn(initialScale = 0.9f),
                exit    = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How well did you know it?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SrsButton(Icons.Default.Close, "Forgot", Color(0xFFF44336), Color(0xFFFFEBEE)) { playRawSound(context, "incorrect"); viewModel.answer(SrsEngine.Quality.FORGOT) }
                        SrsButton(Icons.Default.Done, "Got It", EmeraldGreen, Color(0xFFE8F5E9)) { playRawSound(context, "correct"); viewModel.answer(SrsEngine.Quality.GOOD) }
                        SrsButton(Icons.Default.Star, "Easy", GoldXP, Color(0xFFFFF8E1)) { playRawSound(context, "easy"); viewModel.answer(SrsEngine.Quality.EASY) }
                    }
                }
            }

            this@Column.AnimatedVisibility(
                visible = state.cardPhase == CardPhase.FRONT,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Text(
                    text = "Tap the card to reveal translation",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ListeningFlipExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem) {
    val context = LocalContext.current

    LaunchedEffect(exercise.word.wordId) {
        playWordAudio(context, exercise.word.audioPath)
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FlipCard(
            item        = exercise.word,
            isRevealed  = state.cardPhase == CardPhase.REVEALED,
            blurContent = false, 
            audioOnlyFront = true,
            showNativeFirst = false,
            onClick     = viewModel::revealCard,
            onPlayAudio = { playWordAudio(context, exercise.word.audioPath) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = state.cardPhase == CardPhase.REVEALED,
                enter   = fadeIn() + scaleIn(initialScale = 0.9f),
                exit    = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SrsButton(Icons.Default.Close, "Forgot", Color(0xFFF44336), Color(0xFFFFEBEE)) { playRawSound(context, "incorrect"); viewModel.answer(SrsEngine.Quality.FORGOT) }
                    SrsButton(Icons.Default.Done, "Got It", EmeraldGreen, Color(0xFFE8F5E9)) { playRawSound(context, "correct"); viewModel.answer(SrsEngine.Quality.GOOD) }
                    SrsButton(Icons.Default.Star, "Easy", GoldXP, Color(0xFFFFF8E1)) { playRawSound(context, "easy"); viewModel.answer(SrsEngine.Quality.EASY) }
                }
            }

            this@Column.AnimatedVisibility(
                visible = state.cardPhase == CardPhase.FRONT,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Text(
                    text = "Tap the card to reveal answer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
private fun MultipleChoiceExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem, showWord: Boolean) {
    val promptText = if (showWord) exercise.word.word else exercise.word.translation
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (showWord) "Translate this word" else "How do you say...",
            style = MaterialTheme.typography.labelLarge,
            color = FireOrange,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        LaunchedEffect(exercise.word.wordId) {
            if (showWord) {
                playWordAudio(context, exercise.word.audioPath)
            }
        }

        LaunchedEffect(state.cardPhase) {
            if (state.cardPhase == CardPhase.REVEALED && !showWord) {
                playWordAudio(context, exercise.word.audioPath)
            }
        }

        Text(
            text = promptText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        exercise.options.forEach { option ->
            val optionText = if (showWord) option.translation else option.word
            val isCorrectOption = state.cardPhase == CardPhase.REVEALED && option.wordId == exercise.word.wordId
            val isSelectedAndWrong = state.cardPhase == CardPhase.REVEALED && state.selectedWordId == option.wordId && option.wordId != exercise.word.wordId
            val containerColor = when {
                isSelectedAndWrong -> Color(0xFFEF9A9A)
                isCorrectOption -> FireOrange.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
            val contentColor = when {
                isSelectedAndWrong -> Color(0xFFB71C1C)
                isCorrectOption -> FireOrangeDeep
                else -> MaterialTheme.colorScheme.onSurface
            }
            val borderWidth = if (state.cardPhase == CardPhase.FRONT) 1.dp else if (isCorrectOption || isSelectedAndWrong) 2.dp else 0.dp
            val borderColor = when {
                isSelectedAndWrong -> Color(0xFFE53935)
                isCorrectOption -> FireOrange
                state.cardPhase == CardPhase.FRONT -> MaterialTheme.colorScheme.outlineVariant
                else -> Color.Transparent
            }
            
            OutlinedButton(
                onClick = { if (state.cardPhase == CardPhase.FRONT) viewModel.answerMultipleChoice(option.wordId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
            ) {
                Text(optionText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ListeningChoiceExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem) {
    val context = LocalContext.current
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("What did you hear?", style = MaterialTheme.typography.labelLarge, color = FireOrange, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(32.dp))

        LaunchedEffect(exercise.word.wordId) {
            playWordAudio(context, exercise.word.audioPath)
        }
        
        Surface(
            shape = CircleShape,
            color = FireOrangeDeep,
            modifier = Modifier
                .size(140.dp)
                .clickable { playWordAudio(context, exercise.word.audioPath) }
                .shadow(16.dp, CircleShape, spotColor = FireOrange.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        exercise.options.forEach { option ->
            val isCorrectOption = state.cardPhase == CardPhase.REVEALED && option.wordId == exercise.word.wordId
            val isSelectedAndWrong = state.cardPhase == CardPhase.REVEALED && state.selectedWordId == option.wordId && option.wordId != exercise.word.wordId
            val containerColor = when {
                isSelectedAndWrong -> Color(0xFFEF9A9A)
                isCorrectOption -> FireOrange.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
            val contentColor = when {
                isSelectedAndWrong -> Color(0xFFB71C1C)
                isCorrectOption -> FireOrangeDeep
                else -> MaterialTheme.colorScheme.onSurface
            }
            val borderWidth = if (state.cardPhase == CardPhase.FRONT) 1.dp else if (isCorrectOption || isSelectedAndWrong) 2.dp else 0.dp
            val borderColor = when {
                isSelectedAndWrong -> Color(0xFFE53935)
                isCorrectOption -> FireOrange
                state.cardPhase == CardPhase.FRONT -> MaterialTheme.colorScheme.outlineVariant
                else -> Color.Transparent
            }

            OutlinedButton(
                onClick = { if (state.cardPhase == CardPhase.FRONT) viewModel.answerMultipleChoice(option.wordId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
            ) {
                Text(option.translation, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ListeningTypeExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem) {
    val context = LocalContext.current
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Type what you hear", style = MaterialTheme.typography.labelLarge, color = FireOrange, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(32.dp))

        LaunchedEffect(exercise.word.wordId) {
            playWordAudio(context, exercise.word.audioPath)
        }
        
        Surface(
            shape = CircleShape,
            color = FireOrangeDeep,
            modifier = Modifier
                .size(110.dp)
                .clickable { playWordAudio(context, exercise.word.audioPath) }
                .shadow(16.dp, CircleShape, spotColor = FireOrange.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = Color.White, modifier = Modifier.size(52.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = state.typedText,
            onValueChange = { if (state.cardPhase == CardPhase.FRONT) viewModel.setTypedText(it) },
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(20.dp),
            placeholder = { Text("Enter word...", fontSize = 20.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = FireOrange,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            ),
            isError = state.isTypeCorrect == false
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (state.cardPhase == CardPhase.REVEALED) {
            val isCorrect = state.isTypeCorrect == true
            
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isCorrect) EmeraldGreen.copy(alpha = 0.15f) else Color(0xFFEF9A9A).copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isCorrect) EmeraldGreen else Color(0xFFE53935))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isCorrect) "Awesome! Correct answer 🎉" else "Almost there! Keep trying",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCorrect) EmeraldGreen else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold 
                    )
                    if (!isCorrect) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Correct: ${exercise.word.word}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = viewModel::proceedTypeInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FireOrangeDeep)
            ) {
                Text("CONTINUE", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            }
        } else {
            Button(
                onClick = viewModel::answerTypeInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FireOrangeDeep)
            ) {
                Text("CHECK", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MatchingExercise(state: LearnUiState, viewModel: LearnViewModel, exercise: ExerciseItem) {
    var pairsLeft by remember { mutableStateOf(exercise.options) }
    var errors by remember { mutableStateOf(0) }
    
    val wordCards by remember { mutableStateOf(exercise.options.map { it.wordId to it.word }.shuffled()) }
    val transCards by remember { mutableStateOf(exercise.options.map { it.wordId to it.translation }.shuffled()) }
    
    var selectedWordId by remember { mutableStateOf<Int?>(null) }
    var selectedTransId by remember { mutableStateOf<Int?>(null) }
    
    var matchedIds by remember { mutableStateOf(setOf<Int>()) }
    var errorPair by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(selectedWordId, selectedTransId) {
        if (selectedWordId != null && selectedTransId != null) {
            val wid = selectedWordId!!
            val tid = selectedTransId!!
            
            if (wid == tid) {
                playRawSound(context, "correct")
                matchedIds = matchedIds + wid
                pairsLeft = pairsLeft.filter { it.wordId != wid }
                
                if (pairsLeft.isEmpty()) {
                    delay(300)
                    viewModel.answerMatchingCompleted(errors)
                }
            } else {
                playRawSound(context, "incorrect")
                errors++
                errorPair = wid to tid
                coroutineScope.launch {
                    delay(400)
                    if (errorPair == wid to tid) errorPair = null
                }
            }
            selectedWordId = null
            selectedTransId = null
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Match the pairs", style = MaterialTheme.typography.labelLarge, color = FireOrange, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                wordCards.forEach { (id, text) ->
                    val isMatched = id in matchedIds
                    val isSelected = id == selectedWordId
                    val isError = errorPair?.first == id
                    
                    val bgColor = when {
                        isError -> Color(0xFFEF9A9A)
                        isMatched || isSelected -> FireOrange.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val borderColor = when {
                        isError -> Color(0xFFE53935)
                        isMatched || isSelected -> FireOrange
                        else -> Color.Transparent
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clickable(enabled = !isMatched) { selectedWordId = id },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = androidx.compose.foundation.BorderStroke(if (isError || isMatched || isSelected) 2.dp else 0.dp, borderColor)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isMatched || isSelected) FireOrangeDeep else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transCards.forEach { (id, text) ->
                    val isMatched = id in matchedIds
                    val isSelected = id == selectedTransId
                    val isError = errorPair?.second == id
                    
                    val bgColor = when {
                        isError -> Color(0xFFEF9A9A)
                        isMatched || isSelected -> FireOrange.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val borderColor = when {
                        isError -> Color(0xFFE53935)
                        isMatched || isSelected -> FireOrange
                        else -> Color.Transparent
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clickable(enabled = !isMatched) { selectedTransId = id },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = androidx.compose.foundation.BorderStroke(if (isError || isMatched || isSelected) 2.dp else 0.dp, borderColor)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isMatched || isSelected) FireOrangeDeep else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StreakProgressRing(
    correctToday: Int,
    dailyGoal: Int
) {
    val progress = if (dailyGoal > 0) (correctToday.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val isGoalMet = correctToday >= dailyGoal && dailyGoal > 0
    
    val fireScale by animateFloatAsState(
        targetValue = 1f + (progress * 0.4f), 
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "fireScale"
    )

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "ringProgress"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = strokeWidth)
            )

            if (isGoalMet) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(FireOrange, FireOrangeDeep, FireOrange)),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.6f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = "🔥",
            fontSize = 30.sp,
            modifier = Modifier.graphicsLayer(scaleX = fireScale, scaleY = fireScale)
        )
    }
}


@Composable
private fun FlipCard(
    item: SessionWord,
    isRevealed: Boolean,
    blurContent: Boolean,
    audioOnlyFront: Boolean,
    showNativeFirst: Boolean,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit = {}
) {
    val context = LocalContext.current

    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )
    val isFrontVisible = rotation <= 90f

    val wordTextModifier = if (blurContent) Modifier.blur(16.dp) else Modifier

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                rotationY       = rotation
                cameraDistance  = 16f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        shape     = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = if (isFrontVisible) MaterialTheme.colorScheme.surface else FireOrangeDeep
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isFrontVisible) {
                if (audioOnlyFront) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(shape = CircleShape, color = FireOrange.copy(alpha = 0.15f), modifier = Modifier.size(80.dp).clickable { onPlayAudio() }) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.VolumeUp, "Listen", tint = FireOrange, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        
                        val genderCode = item.gender ?: ""
                        val hasGender = genderCode.isNotBlank() && genderCode != "null"
                        val (genderText, genderColor) = when (genderCode) {
                            "m" -> "MASCULINE" to Color(0xFF0D47A1)
                            "f" -> "FEMININE" to Color(0xFFB71C1C)
                            "n" -> "NEUTER" to Color(0xFF757575)
                            "n_pl" -> "PLURAL" to Color(0xFF757575)
                            else -> genderCode.uppercase() to Color(0xFF757575)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (!showNativeFirst) {
                                if (!item.wordType.isNullOrBlank()) {
                                    BadgeLayout(item.wordType.uppercase(), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (hasGender) {
                                    BadgeLayout(genderText, genderColor, Color.White)
                                }
                            } else {
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!item.article.isNullOrBlank() && item.article != "null" && !blurContent && !showNativeFirst) {
                                Text(
                                    text = "${item.article} ",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = if (showNativeFirst) item.translation else item.word,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = wordTextModifier
                            )
                        }

                        if (!item.plural.isNullOrBlank() && item.plural != "null" && !blurContent && !showNativeFirst) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "pl. ${item.plural}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (!item.exampleSentence.isNullOrBlank() && item.exampleSentence != "null" && !blurContent && !showNativeFirst) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "\"${item.exampleSentence}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (!showNativeFirst) {
                            Surface(shape = CircleShape, color = FireOrange.copy(alpha = 0.1f)) {
                                IconButton(onClick = { onPlayAudio() }) { Icon(Icons.Rounded.VolumeUp, "Pronounce", tint = FireOrange) }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            } else {
                val backWord = if (audioOnlyFront) item.word else if (showNativeFirst) item.word else item.translation
                val backSubWord = if (audioOnlyFront) null else if (showNativeFirst) item.translation else item.word
                val isTargetWordOnBack = audioOnlyFront || showNativeFirst
                
                val genderCode = item.gender ?: ""
                val hasGender = genderCode.isNotBlank() && genderCode != "null"
                val (genderText, genderColor) = when (genderCode) {
                    "m" -> "MASCULINE" to Color(0xFF0D47A1)
                    "f" -> "FEMININE" to Color(0xFFB71C1C)
                    "n" -> "NEUTER" to Color(0xFF757575)
                    "n_pl" -> "PLURAL" to Color(0xFF757575)
                    else -> genderCode.uppercase() to Color(0xFF757575)
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp).fillMaxSize().graphicsLayer { rotationY = 180f },
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isTargetWordOnBack && (!item.wordType.isNullOrBlank() || hasGender)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (!item.wordType.isNullOrBlank()) {
                                BadgeLayout(item.wordType.uppercase(), Color.White.copy(alpha = 0.2f), Color.White)
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (hasGender) {
                                BadgeLayout(genderText, genderColor, Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Text(if (audioOnlyFront) "Word" else "Translation", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTargetWordOnBack && !item.article.isNullOrBlank() && item.article != "null") {
                            Text(
                                text = "${item.article} ",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = backWord.ifBlank { "—" },
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isTargetWordOnBack && !item.plural.isNullOrBlank() && item.plural != "null") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "pl. ${item.plural}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    if (backSubWord != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.15f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                                if (!isTargetWordOnBack && !item.article.isNullOrBlank() && item.article != "null") {
                                    Text(
                                        text = "${item.article} ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = backSubWord,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else if (audioOnlyFront) {
                        Spacer(modifier = Modifier.height(24.dp))
                        if (!item.translation.isNullOrBlank()) {
                            Text(
                                text = item.translation,
                                style = MaterialTheme.typography.titleMedium,
                                fontStyle = FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (isTargetWordOnBack && !item.exampleSentence.isNullOrBlank() && item.exampleSentence != "null") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "\"${item.exampleSentence}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isTargetWordOnBack && !item.wordType.isNullOrBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeLayout(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor.copy(alpha = if (containerColor.alpha > 0.8f) 0.6f else containerColor.alpha),
        modifier = Modifier.height(26.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
private fun SrsButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, surfaceColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = surfaceColor,
            modifier = Modifier
                .size(72.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = color, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
    }
}


@Composable
private fun SessionFinishedScreen(
    correctCount: Int,
    forgotCount: Int,
    total: Int,
    engineResult: EngineResult?,
    onFinishClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playRawSound(context, "end")
    }
    
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    
    val xp = engineResult?.xpGranted ?: 0
    val achievements = engineResult?.newAchievements ?: emptyList()

    val accuracy = if (total > 0) (correctCount * 100f / total).toInt() else 0
    val streakExtended = engineResult?.streakUpdated == true
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
            if (streakExtended) {
                AnimatedStreakFlame()
            } else {
                Text(emoji, fontSize = 100.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Session Complete!",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color      = Color.White
            )
            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FinishStat(label = "Correct",  value = "$correctCount", color = Color(0xFFC8E6C9))
                    FinishStat(label = "Forgot",   value = "$forgotCount",  color = Color(0xFFFFCDD2))
                    FinishStat(label = "Accuracy", value = "$accuracy%",    color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (xp > 0 || achievements.isNotEmpty()) {
                if (xp > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡", fontSize = 24.sp)
                            Text(
                                text = "+$xp XP",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
                if (achievements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(achievements) { achievement ->
                            AchievementBadge(achievement) { selectedAchievement = it }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(64.dp))
            }

            Button(
                onClick = {
                    val streakUpdated = engineResult?.streakUpdated == true
                    if (streakUpdated) {
                        playRawSound(context, "burn")
                    }
                    onFinishClick(streakUpdated)
                },
                colors  = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape   = CircleShape,
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(0.85f)
                    .shadow(16.dp, CircleShape, spotColor = Color.White.copy(alpha = 0.5f))
            ) {
                Text(
                    "NEXT",
                    color      = FireOrangeDeep,
                    fontWeight = FontWeight.Black,
                    fontSize   = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        
        selectedAchievement?.let { achievement ->
            AchievementDialog(
                achievement = achievement,
                onDismiss = { selectedAchievement = null }
            )
        }
    }
}

@Composable
private fun FinishStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = color)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
    }
}


@Composable
private fun AnimatedStreakFlame(modifier: Modifier = Modifier) {
    val flameScale  = remember { Animatable(0.2f) }
    val glowBloom   = remember { Animatable(0f)   }
    val glowAlpha   = remember { Animatable(0f)   }

    LaunchedEffect(Unit) {
        launch {
            glowAlpha.animateTo(1f, tween(500))
            glowBloom.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow
                )
            )
        }
        flameScale.animateTo(
            1.45f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        flameScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flameLive")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.13f,
        animationSpec = infiniteRepeatable(
            animation  = tween(720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBrightness"
    )

    Box(
        modifier          = modifier.size(180.dp),
        contentAlignment  = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxR     = size.minDimension / 2f
            val bloom    = glowBloom.value
            val alpha    = glowAlpha.value
            val vivid    = glowPulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF6D00).copy(alpha = 0.50f * alpha * vivid),
                        Color(0xFFFF9800).copy(alpha = 0.28f * alpha * vivid),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxR
                ),
                radius = maxR * bloom
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD740).copy(alpha = 0.80f * alpha * vivid),
                        Color(0xFFFF6D00).copy(alpha = 0.40f * alpha * vivid),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxR * 0.55f
                ),
                radius = maxR * 0.55f * bloom
            )
        }

        Text(
            text     = "🔥",
            fontSize = 90.sp,
            modifier = Modifier.graphicsLayer {
                val s = flameScale.value * pulseFactor
                scaleX = s
                scaleY = s
            }
        )
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement, onClick: (Achievement) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp)
            .clickable { onClick(achievement) }
    ) {
        val context = LocalContext.current
        val titleName = achievement.title.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
        val imageName = "ach_$titleName"
        val imageResId = remember(imageName) {
            context.resources.getIdentifier(imageName, "drawable", context.packageName)
        }
        
        if (imageResId != 0) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = imageResId),
                contentDescription = achievement.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        } else {
            Text(achievement.icon.ifEmpty { "🏆" }, fontSize = 56.sp)
        }
    }
}

@Composable
private fun AchievementDialog(achievement: Achievement, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = FireOrange.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val context = LocalContext.current
                        val titleName = achievement.title.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
                        val imageName = "ach_$titleName"
                        val imageResId = remember(imageName) {
                            context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        }
                        
                        if (imageResId != 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = imageResId),
                                contentDescription = achievement.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                colorFilter = if (!achievement.unlocked) 
                                    androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
                                    ) else null
                            )
                        } else {
                            Text(achievement.icon.ifEmpty { "🏆" }, fontSize = 40.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = achievement.title.ifEmpty { "Achievement" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (!achievement.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private var activeRawPlayer: android.media.MediaPlayer? = null
private var activeWordPlayer: android.media.MediaPlayer? = null

fun playTestSound(context: android.content.Context) {
    try {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, uri)
        r.play()
    } catch (e: Exception) {
    }
}

fun playRawSound(context: android.content.Context, soundName: String) {
    try {
        activeRawPlayer?.release()
        activeRawPlayer = null
        
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        if (resId != 0) {
            val mediaPlayer = android.media.MediaPlayer.create(context, resId)
            activeRawPlayer = mediaPlayer
            mediaPlayer?.setOnCompletionListener { 
                it.release()
                if (activeRawPlayer == it) activeRawPlayer = null
            }
            mediaPlayer?.start()
        }
    } catch (e: Exception) {
    }
}

fun playWordAudio(context: android.content.Context, audioPath: String?) {
    if (audioPath.isNullOrBlank()) return
    try {
        activeWordPlayer?.release()
        activeWordPlayer = null

        val afd = context.assets.openFd(audioPath)
        val mediaPlayer = android.media.MediaPlayer()
        activeWordPlayer = mediaPlayer
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer.setOnCompletionListener { 
            it.release()
            if (activeWordPlayer == it) activeWordPlayer = null
        }
        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (e: Exception) {
    }
}
