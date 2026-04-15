package com.example.langfire_app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val achievements = uiState.achievements
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "${achievements.count { it.unlocked }} / ${achievements.size} Unlocked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (achievements.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FireOrange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                items(achievements) { achievement ->
                    DetailedAchievementCard(
                        achievement = achievement,
                        onClick = { selectedAchievement = achievement }
                    )
                }
            }
        }
    }

    selectedAchievement?.let { achievement ->
        AchievementDetailDialog(
            achievement = achievement,
            onDismiss = { selectedAchievement = null }
        )
    }
}

@Composable
private fun DetailedAchievementCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val isUnlocked = achievement.unlocked
    val context = LocalContext.current
    val titleName = achievement.title.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
    val imageName = "ach_$titleName"
    val imageResId = remember(imageName) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                Color.White
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) FireOrange.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = achievement.title,
                        modifier = Modifier
                            .size(108.dp)
                            .graphicsLayer { alpha = if (isUnlocked) 1f else 0.5f },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = achievement.icon.ifEmpty { "🏅" },
                        fontSize = 80.sp,
                        modifier = Modifier.graphicsLayer { alpha = if (isUnlocked) 1f else 0.5f }
                    )
                }

                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp, end = 8.dp)
                            .size(24.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievement.title.ifEmpty { achievement.type },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUnlocked) FireOrangeDeep else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = achievement.description ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 42.dp)
                )
            }
        }
    }
}

@Composable
private fun AchievementDetailDialog(achievement: Achievement, onDismiss: () -> Unit) {
    val isUnlocked = achievement.unlocked
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("GOT IT", fontWeight = FontWeight.Bold, color = FireOrangeDeep)
            }
        },
        icon = {
            val context = LocalContext.current
            val titleName = achievement.title.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
            val imageName = "ach_$titleName"
            val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(160.dp).graphicsLayer { alpha = if (isUnlocked) 1f else 0.5f },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        achievement.icon.ifEmpty { "🏆" }, 
                        fontSize = 80.sp,
                        modifier = Modifier.graphicsLayer { alpha = if (isUnlocked) 1f else 0.5f }
                    )
                }
            }
        },
        title = {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = achievement.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
