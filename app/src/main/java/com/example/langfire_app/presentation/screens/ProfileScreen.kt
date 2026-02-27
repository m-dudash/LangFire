package com.example.langfire_app.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.CourseLevelInfo
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.ProfileUiState
import com.example.langfire_app.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLibraryClick: () -> Unit = {},
    onBurnClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LangFireBottomBar(
                onLibraryClick = onLibraryClick,
                onBurnClick = onBurnClick,
                onProfileClick = onProfileClick,
                initialSelected = NavTab.PROFILE
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                !uiState.hasProfile -> {
                    RegistrationForm(
                        courses = uiState.availableCourses,
                        onRegister = viewModel::onRegister
                    )
                }
                else -> {
                    ProfileContent(uiState = uiState)
                }
            }
        }
    }
}

@Composable
fun RegistrationForm(
    courses: List<Course>,
    onRegister: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FireOrange.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text("🔥", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to LangFire",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = FireOrange
                )
            )
            Text(
                text = "Ignite your language learning journey",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("What should we call you?") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FireOrange,
                    focusedLabelColor = FireOrange,
                    cursorColor = FireOrange
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Course Selection
            Text(
                text = "Choose your path:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
            ) {
                items(courses) { course ->
                    CourseSelectionCard(
                        course = course,
                        isSelected = selectedCourseId == course.id,
                        onClick = { selectedCourseId = course.id }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Button
            Button(
                onClick = { selectedCourseId?.let { onRegister(name, it) } },
                enabled = name.isNotBlank() && selectedCourseId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FireOrange,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Start Learning",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CourseSelectionCard(
    course: Course,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) FireOrange else Color.Transparent
    val backgroundColor = if (isSelected) FireOrange.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square cards
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.icon,
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = FireOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROFILE CONTENT
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileContent(uiState: ProfileUiState) {
    val profile = uiState.profile ?: return
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item { ProfileHeroSection(profile = profile) }
        item { Spacer(Modifier.height(20.dp)) }
        item { StatsSection(uiState = uiState) }
        item { Spacer(Modifier.height(20.dp)) }
        item { AccuracySection(uiState = uiState) }
        item { Spacer(Modifier.height(20.dp)) }
        item { AchievementsSection(achievements = uiState.achievements) }
        item { Spacer(Modifier.height(20.dp)) }
        item { CourseLanguageLevelsSection(courseProgress = uiState.courseProgress) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO SECTION: Avatar + Name + Streak
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroSection(profile: Profile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Background gradient wash
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FireOrange.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circle with initials
            val initials = profile.name
                .trim()
                .split(" ")
                .take(2)
                .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                .ifEmpty { "?" }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(FireOrange, FireOrangeDeep)
                        )
                    )
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Name + streak badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(10.dp))
                ProfileStreakBadge(streakDays = profile.streakDays)
            }
        }
    }
}

@Composable
private fun ProfileStreakBadge(streakDays: Int) {
    val isAlive = streakDays > 0
    val accentColor = if (isAlive) Color(0xFFFF6D00) else Color(0xFF9E9E9E)
    Surface(
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = if (isAlive) "🔥" else "💤", fontSize = 14.sp)
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS SECTION: XP, Words, Toughest Word, Correct, Errors
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsSection(uiState: ProfileUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = "Statistics")

        // XP + Words learned row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = "⚡",
                label = "Total XP",
                value = formatXpStat(uiState.profile?.xp ?: 0),
                accentColor = GoldXP,
                containerColor = GoldContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = "📚",
                label = "Words Learned",
                value = "${uiState.wordsLearned}",
                accentColor = EmeraldGreen,
                containerColor = EmeraldContainer
            )
        }

        // Toughest word
        uiState.toughestWord?.let { ToughestWordCard(word = it) }

        // Correct + Error row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = "✅",
                label = "Correct",
                value = "${uiState.totalCorrect}",
                accentColor = EmeraldGreen,
                containerColor = EmeraldContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = "❌",
                label = "Errors",
                value = "${uiState.totalErrors}",
                accentColor = Color(0xFFD32F2F),
                containerColor = Color(0xFFFFCDD2)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    accentColor: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun ToughestWordCard(word: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FireContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "😤", fontSize = 28.sp)
            Column {
                Text(
                    text = "Toughest Word to Learn",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnFireContainer.copy(alpha = 0.65f)
                )
                Text(
                    text = word,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OnFireContainer
                    )
                )
            }
        }
    }
}

private fun formatXpStat(xp: Int): String = when {
    xp >= 1_000_000 -> "${xp / 1_000_000}M"
    xp >= 1000 -> {
        val k = xp / 1000
        val r = (xp % 1000) / 100
        if (r == 0) "${k}K" else "${k}.${r}K"
    }
    else -> "$xp"
}

// ─────────────────────────────────────────────────────────────────────────────
// ACCURACY SECTION: animated progress bar 0→100
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AccuracySection(uiState: ProfileUiState) {
    val accuracy = uiState.accuracyPercent
    val animatedProgress by animateFloatAsState(
        targetValue = (accuracy / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "accuracy_progress"
    )

    val accentColor = when {
        accuracy >= 80f -> EmeraldGreen
        accuracy >= 50f -> GoldXP
        else            -> Color(0xFFD32F2F)
    }
    val barColors = when {
        accuracy >= 80f -> listOf(Color(0xFF66BB6A), EmeraldGreen)
        accuracy >= 50f -> listOf(GoldXP, FireOrange)
        else            -> listOf(Color(0xFFEF9A9A), Color(0xFFD32F2F))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accuracy Rate",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${accuracy.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                )
            }

            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                // Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(barColors))
                )
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACHIEVEMENTS SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementsSection(achievements: List<Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = "Achievements")

        if (achievements.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏆", fontSize = 28.sp)
                    Text(
                        text = "Keep learning to unlock achievements!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementBadge(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    val isUnlocked = achievement.unlocked
    val bgColor    = if (isUnlocked) FortuneContainer
                     else MaterialTheme.colorScheme.surfaceVariant

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isUnlocked)
            BorderStroke(1.5.dp, FortunePurple.copy(alpha = 0.55f))
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 2.dp else 0.dp
        ),
        modifier = Modifier.width(90.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = achievement.icon.ifEmpty { "🏅" },
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isUnlocked) 1f else 0.32f
                }
            )
            Text(
                text  = achievement.title.ifEmpty { achievement.type },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked)
                        FortunePurple
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                ),
                textAlign = TextAlign.Center,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COURSE LANGUAGE LEVELS SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CourseLanguageLevelsSection(courseProgress: List<CourseLevelInfo>) {
    if (courseProgress.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = "Language Levels")
        courseProgress.forEach { info -> CourseLevelCard(info = info) }
    }
}

@Composable
private fun CourseLevelCard(info: CourseLevelInfo) {
    val isComplete  = info.targetLevel == null
    val progress    = if (info.totalWordsInTarget > 0)
        info.wordsLearnedInTarget.toFloat() / info.totalWordsInTarget
    else if (isComplete) 1f else 0f

    val animatedProgress by animateFloatAsState(
        targetValue    = progress.coerceIn(0f, 1f),
        animationSpec  = tween(1000, easing = FastOutSlowInEasing),
        label          = "level_progress"
    )

    val achievedGradient = profileLevelGradient(info.achievedLevel ?: "")
    val targetGradient   = profileLevelGradient(info.targetLevel   ?: info.achievedLevel ?: "")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header: icon + course name ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isComplete) targetGradient
                            else achievedGradient.takeIf { info.achievedLevel != null }
                                ?: Brush.verticalGradient(listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                ))
                        )
                ) {
                    Text(text = info.courseIcon, fontSize = 24.sp)
                }
                Column {
                    Text(
                        text  = info.courseName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = info.targetLang.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // ── Level badges + progress bar ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left badge: achieved level (colored) or empty slot
                LevelBadge(
                    label    = info.achievedLevel ?: "--",
                    gradient = achievedGradient,
                    colored  = info.achievedLevel != null
                )

                // Progress bar
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isComplete)
                                        targetGradient
                                    else
                                        achievedGradient.takeIf { info.achievedLevel != null }
                                            ?: Brush.horizontalGradient(
                                                listOf(FireOrange, FireOrangeDeep)
                                            )
                                )
                        )
                    }
                    // Word count sub-label
                    val subLabel = when {
                        isComplete -> "All levels mastered 🏆"
                        info.totalWordsInTarget == 0 -> "No words yet"
                        else -> "${info.wordsLearnedInTarget} / ${info.totalWordsInTarget} words"
                    }
                    Text(
                        text  = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                // Right badge: target level (grey) or C2 done
                LevelBadge(
                    label    = info.targetLevel ?: info.achievedLevel ?: "C2",
                    gradient = targetGradient,
                    colored  = isComplete
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(label: String, gradient: Brush, colored: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (colored) gradient
                else Brush.verticalGradient(
                    listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
                )
            )
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight    = FontWeight.Black,
                color         = Color.White,
                letterSpacing = (-0.5).sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

private fun profileLevelGradient(level: String): Brush = when (level.uppercase()) {
    "A1" -> Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))
    "A2" -> Brush.verticalGradient(listOf(Color(0xFF26A69A), Color(0xFF00695C)))
    "B1" -> Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0)))
    "B2" -> Brush.verticalGradient(listOf(Color(0xFF7E57C2), Color(0xFF4527A0)))
    "C1" -> Brush.verticalGradient(listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)))
    "C2" -> Brush.linearGradient(listOf(Color(0xFFFFD740), Color(0xFFFF6F00)))
    else -> Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray))
}
