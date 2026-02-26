package com.example.langfire_app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.presentation.ui.theme.FireOrange
import com.example.langfire_app.presentation.ui.theme.GoldXP
import com.example.langfire_app.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.hasProfile) {
        RegistrationForm(
            courses = uiState.availableCourses,
            onRegister = viewModel::onRegister
        )
    } else {
        uiState.userProfile?.let { profile ->
            UserProfileScreen(
                profile = profile,
                onSettingsClick = { /* TODO: navigate to settings */ }
            )
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
