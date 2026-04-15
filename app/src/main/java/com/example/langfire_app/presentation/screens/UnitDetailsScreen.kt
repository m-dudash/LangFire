package com.example.langfire_app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.langfire_app.domain.model.UnitWordItem
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.UnitDetailsViewModel
import com.example.langfire_app.presentation.components.DeveloperSignature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailsScreen(
    viewModel: UnitDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FireOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                UnitDetailsHeader(
                    unitName = uiState.unitName,
                    onBackClick = onBackClick
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.words.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No words found in this unit.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    items(uiState.words) { wordItem ->
                        WordItemCard(
                            wordItem = wordItem,
                            onAlreadyKnow = { viewModel.markWord(wordItem.wordId, 1.0f) },
                            onLearnIt = { viewModel.markWord(wordItem.wordId, 0.0f) },
                            onClearWord = { viewModel.clearWord(wordItem.wordId) }
                        )
                    }
                    item { DeveloperSignature() }
                }
            }
        }
    }
}

@Composable
fun UnitDetailsHeader(
    unitName: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FireOrange.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(FireOrange, FireOrangeDeep)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = unitName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Browse unit vocabulary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WordItemCard(
    wordItem: UnitWordItem,
    onAlreadyKnow: () -> Unit,
    onLearnIt: () -> Unit,
    onClearWord: () -> Unit
) {
    val coeff = wordItem.knowledgeCoeff
    val isMarked = coeff != null
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            
            val leftBg = if (isMarked) MaterialTheme.colorScheme.surfaceVariant else EmeraldContainer
            val leftContentColor = if (isMarked) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldGreen
            
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(leftBg)
                    .clickable { if (isMarked) onClearWord() else onAlreadyKnow() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isMarked) Icons.Default.Clear else Icons.Default.Check,
                        contentDescription = if (isMarked) "Clear word" else "Already Know",
                        tint = leftContentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMarked) "Clear word" else "I Know",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = leftContentColor,
                        fontSize = 10.sp
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var wordFontSize by remember { mutableStateOf(22.sp) }
                    Text(
                        text = wordItem.word,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = wordFontSize,
                            lineHeight = wordFontSize * 1.2f
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow && wordFontSize > 10.sp) {
                                wordFontSize *= 0.9f
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var translationFontSize by remember { mutableStateOf(14.sp) }
                    Text(
                        text = wordItem.translation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = translationFontSize,
                            lineHeight = translationFontSize * 1.2f
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow && translationFontSize > 8.sp) {
                                translationFontSize *= 0.9f
                            }
                        }
                    )

                    if (!wordItem.audioPath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = FireOrange.copy(alpha = 0.1f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            IconButton(
                                onClick = { playWordAudio(context, wordItem.audioPath) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.VolumeUp,
                                    contentDescription = "Play audio",
                                    tint = FireOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (isMarked) {
                Box(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val progressVal = coeff ?: 0f
                    val percent = (progressVal * 100).toInt()
                    val color = when {
                        progressVal >= 0.8f -> EmeraldGreen
                        progressVal >= 0.4f -> GoldXP
                        else -> FireOrange
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier.size(48.dp),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .background(Brush.linearGradient(listOf(FireOrange, FireOrangeDeep)))
                        .clickable { onLearnIt() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocalFireDepartment, 
                            contentDescription = "Learn It", 
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learn it!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
