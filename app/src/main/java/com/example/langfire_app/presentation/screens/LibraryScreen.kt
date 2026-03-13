package com.example.langfire_app.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.langfire_app.domain.model.LibraryLevelSection
import com.example.langfire_app.domain.model.LibraryUnit
import com.example.langfire_app.presentation.screens.LangFireBottomBar
import com.example.langfire_app.presentation.screens.NavTab
import com.example.langfire_app.presentation.ui.theme.*
import com.example.langfire_app.presentation.viewmodels.LibraryViewModel

// ─────────────────────────────────────────────────────────────────────────────
// LIBRARY SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    courseId: Int? = null,
    viewModel: LibraryViewModel = hiltViewModel(),
    onUnitClick: (Int) -> Unit = {},
    onBurnClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
) {
    val sections by viewModel.state.collectAsState()
    val courseInfo by viewModel.courseInfo.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadLibrary(courseId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LangFireBottomBar(
                onLibraryClick = onLibraryClick,
                onBurnClick = onBurnClick,
                onProfileClick = onProfileClick,
                initialSelected = NavTab.LIBRARY
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Header ──────────────────────────────────────────────
            item {
                LibraryHeader(
                    courseFlag = courseInfo?.icon ?: "🌐",
                    courseName = courseInfo?.name ?: "Course",
                    totalUnits = sections.sumOf { it.units.size },
                    completedUnits = sections.sumOf { s -> s.units.count { it.isCompleted } }
                )
            }

            // ── Empty state ──────────────────────────────────────────
            if (sections.isEmpty()) {
                item {
                    LibraryEmptyState()
                }
            }

            // ── Sections ─────────────────────────────────────────────
            items(sections) { section ->
                LevelSection(
                    section = section,
                    onUnitClick = onUnitClick
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIBRARY HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LibraryHeader(
    courseFlag: String,
    courseName: String,
    totalUnits: Int,
    completedUnits: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Decorative gradient wash at top
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
            // Title row with icon
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
                        imageVector = Icons.Outlined.LibraryBooks,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your vocabulary units",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // NEW: Course Badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FireOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(courseFlag, fontSize = 16.sp)
                        Text(
                            text = courseName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress card
            if (totalUnits > 0) {
                LibraryProgressCard(
                    totalUnits = totalUnits,
                    completedUnits = completedUnits
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROGRESS CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LibraryProgressCard(
    totalUnits: Int,
    completedUnits: Int
) {
    val progress = if (totalUnits > 0) completedUnits.toFloat() / totalUnits else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "library_progress"
    )
    val progressPercent = (animatedProgress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedUnits of $totalUnits units completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Percentage bubble
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(FireOrange, FireOrangeDeep))
                        )
                ) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            // Progress bar
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
                            Brush.horizontalGradient(
                                listOf(FireOrange, FireOrangeDeep)
                            )
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LibraryEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "📚", fontSize = 72.sp)
        Text(
            text = "No units yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your library is empty. Start a course to populate your units.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LevelSection(
    section: LibraryLevelSection,
    onUnitClick: (Int) -> Unit
) {
    val sectionAlpha = if (section.isLocked) 0.48f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(sectionAlpha)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 4.dp)
    ) {
        // ── Section header ────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            // Level badge
            val levelGradient = libraryLevelGradient(section.levelName)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(levelGradient)
            ) {
                Text(
                    text = section.levelName,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Level ${section.levelName}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            // Lock / unit count chip
            if (section.isLocked) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Locked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                val completedCount = section.units.count { it.isCompleted }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (completedCount == section.units.size && section.units.isNotEmpty())
                        EmeraldContainer else FireContainer
                ) {
                    Text(
                        text = "$completedCount/${section.units.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (completedCount == section.units.size && section.units.isNotEmpty())
                            EmeraldGreen else OnFireContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ── Unit grid (2 columns via chunked rows) ────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            section.units.chunked(2).forEach { rowUnits ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (unit in rowUnits) {
                        UnitCard(
                            unit = unit,
                            isLocked = section.isLocked,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!section.isLocked) onUnitClick(unit.id) }
                        )
                    }
                    if (rowUnits.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Divider between sections
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            thickness = 1.dp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UNIT CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun UnitCard(
    unit: LibraryUnit,
    isLocked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val progress = if (unit.totalWords > 0)
        unit.learnedWords.toFloat() / unit.totalWords else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "unit_progress_${unit.id}"
    )

    // Color scheme per completion state
    val isCompleted = unit.isCompleted
    val cardBackground = when {
        isCompleted -> EmeraldContainer.copy(alpha = 0.55f)
        isLocked    -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else        -> MaterialTheme.colorScheme.surface
    }
    val accentColor = when {
        isCompleted -> EmeraldGreen
        isLocked    -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else        -> FireOrange
    }
    val borderBrush: Brush? = when {
        isCompleted -> Brush.linearGradient(listOf(EmeraldGreen.copy(alpha = 0.55f), EmeraldGreen.copy(alpha = 0.2f)))
        !isLocked && progress > 0f -> Brush.linearGradient(listOf(FireOrange.copy(alpha = 0.55f), FireOrangeDeep.copy(alpha = 0.25f)))
        else -> null
    }

    Card(
        modifier = modifier
            .aspectRatio(0.95f)
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!isLocked && !isCompleted) 3.dp else 0.dp
        ),
        border = borderBrush?.let {
            androidx.compose.foundation.BorderStroke(1.5.dp, it)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle top gradient accent on unlocked cards
            if (!isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: completion icon or unlock icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    when {
                        isCompleted -> Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        isLocked -> Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle: unit name
                var titleFontSize by remember { mutableStateOf(14.sp) }
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize,
                        lineHeight = titleFontSize * 1.2f
                    ),
                    color = if (isLocked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow && titleFontSize > 10.sp) {
                            titleFontSize *= 0.9f
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom: progress bar + word count
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isLocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                    ) {
                        if (!isLocked && animatedProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isCompleted)
                                            Brush.horizontalGradient(listOf(EmeraldGreen, EmeraldGreen))
                                        else
                                            Brush.horizontalGradient(listOf(FireOrange, FireOrangeDeep))
                                    )
                            )
                        }
                    }

                    // Word count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unit.learnedWords}/${unit.totalWords}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = accentColor,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "learned words",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL GRADIENT HELPER (mirrors HomeScreen level gradients)
// ─────────────────────────────────────────────────────────────────────────────
private fun libraryLevelGradient(level: String): Brush = when (level.uppercase()) {
    "A1" -> Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))
    "A2" -> Brush.verticalGradient(listOf(Color(0xFF26A69A), Color(0xFF00695C)))
    "B1" -> Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0)))
    "B2" -> Brush.verticalGradient(listOf(Color(0xFF7E57C2), Color(0xFF4527A0)))
    "C1" -> Brush.verticalGradient(listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A)))
    "C2" -> Brush.linearGradient(listOf(Color(0xFFFFD740), Color(0xFFFF6F00)))
    else -> Brush.verticalGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100)))
}