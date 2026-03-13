package com.example.langfire_app.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.presentation.ui.theme.FireOrange
import com.example.langfire_app.presentation.ui.theme.FireOrangeDeep
import com.example.langfire_app.presentation.viewmodels.ProfileViewModel

@Composable
fun RegistrationScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistrationForm(
        courses = uiState.availableCourses,
        onRegister = { name, courseId, goal, avatar ->
            viewModel.onRegister(name, courseId, goal, avatar)
            onRegistrationSuccess()
        }
    )
}

@Composable
fun RegistrationForm(
    courses: List<Course>,
    onRegister: (String, Int, Int, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    
    // Goal Slider logic: Beginner (15), Moderate (25), Intensive (35), BURN IT (50)
    val goalTiers = Profile.GOAL_TIERS
    var sliderPosition by remember { mutableStateOf(1f) } // Default to Moderate (index 1)
    val selectedGoal = goalTiers[sliderPosition.toInt().coerceIn(0, goalTiers.lastIndex)]

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if(uri != null) avatarUri = uri }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Background Accents
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FireOrange.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("🔥", fontSize = 72.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Create Profile",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = FireOrangeDeep
                    )
                )
                Text(
                    text = "Fuel your learning with goals",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(40.dp))
            }

            // Avatar
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, FireOrange.copy(alpha = 0.5f), CircleShape)
                        .clickable { 
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            ) 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = FireOrange)
                            Text("Photo", style = MaterialTheme.typography.labelSmall, color = FireOrange)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // Inputs (Wrapped in padding because they are full width)
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("How should we call you?") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FireOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedLabelColor = FireOrange
                        )
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Daily Goal Slider
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = FireOrange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$selectedGoal answers",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                    }
                    
                    val goalLabels = listOf("🌱", "🤔", "🔥", "💀")
                    
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when(selectedGoal) {
                            Profile.GOAL_BEGINNER -> "Beginner - Just starting out"
                            Profile.GOAL_MODERATE -> "📈 Progressor - Steady growth"
                            Profile.GOAL_INTENSIVE -> "Intensive - Feeling the heat"
                            Profile.GOAL_BURN -> "BURN IT! - Mastery or bust"
                            else -> "Custom goal"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedGoal == Profile.GOAL_BURN) FireOrangeDeep else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..3f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = FireOrangeDeep,
                            activeTrackColor = FireOrange,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }

            // Course Selection
            item {
                Text(
                    text = "Choose your course",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    courses.chunked(2).forEach { rowCourses ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowCourses.forEach { course ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CourseSelectionCard(
                                        course = course,
                                        isSelected = selectedCourseId == course.id,
                                        onClick = { selectedCourseId = course.id }
                                    )
                                }
                            }
                            if (rowCourses.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // The Action Button is now INSIDE the scrollable list
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { 
                        selectedCourseId?.let { courseId ->
                            onRegister(name, courseId, selectedGoal, avatarUri?.toString()) 
                        }
                    },
                    enabled = name.isNotBlank() && selectedCourseId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FireOrangeDeep,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "Ignite Your Journey",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
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
    val backgroundColor = if (isSelected) FireOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
                fontSize = 44.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FireOrangeDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
