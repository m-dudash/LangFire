package com.example.langfire_app.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private fun copyUriToRegistrationStorage(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "avatars").also { it.mkdirs() }
        val dest = File(dir, "avatar_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
fun RegistrationScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistrationForm(
        uiState = uiState,
        onRegister = { email, pass, name, courseId, goal, avatar ->
            viewModel.onRegister(email, pass, name, courseId, goal, avatar)
        },
        onLogin = { email, pass ->
            viewModel.onLogin(email, pass)
        }
    )
    
    LaunchedEffect(uiState.hasProfile) {
        if (uiState.hasProfile) {
            onRegistrationSuccess()
        }
    }
}

@Composable
fun RegistrationForm(
    uiState: com.example.langfire_app.presentation.viewmodels.ProfileUiState,
    onRegister: (String, String, String, Int, Int, String?) -> Unit,
    onLogin: (String, String) -> Unit
) {
    val context = LocalContext.current
    var isLoginMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    var selectedCourseId by remember { mutableStateOf<Int?>(null) }
    var avatarFilePath by remember { mutableStateOf<String?>(null) }
    
    val goalTiers = Profile.GOAL_TIERS
    var sliderPosition by remember { mutableStateOf(1f) }
    val selectedGoal = goalTiers[sliderPosition.toInt().coerceIn(0, goalTiers.lastIndex)]

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val copied = copyUriToRegistrationStorage(context, uri)
                if (copied != null) avatarFilePath = copied
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    text = if (isLoginMode) "Welcome Back" else "Create Profile",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = FireOrangeDeep
                    )
                )
                Text(
                    text = if (isLoginMode) "Login to restore your progress" else "Fuel your learning with goals",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isLoginMode) FireOrange else Color.Transparent)
                            .clickable { isLoginMode = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "New Pilot",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!isLoginMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isLoginMode) FireOrange else Color.Transparent)
                            .clickable { isLoginMode = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Veteran",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLoginMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("pilot@langfire.io") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FireOrange,
                            focusedLabelColor = FireOrange
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FireOrange,
                            focusedLabelColor = FireOrange
                        )
                    )

                    if (password.isNotEmpty() && password.length < 6) {
                        Text(
                            text = "Password must be at least 6 characters",
                            color = FireOrange,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    if (uiState.authError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = uiState.authError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            if (!isLoginMode) {
                item {
                    Spacer(Modifier.height(32.dp))
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
                        if (avatarFilePath != null) {
                            AsyncImage(
                                model = avatarFilePath,
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
                                focusedLabelColor = FireOrange
                            )
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }

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
                        uiState.availableCourses.chunked(2).forEach { rowCourses ->
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
            }
            item {
                Spacer(Modifier.height(24.dp))
                val isEmailValid = email.contains("@") && email.contains(".")
                val isPasswordValid = password.length >= 6
                val isRegistrationValid = isEmailValid && isPasswordValid && name.isNotBlank() && selectedCourseId != null
                val isLoginValid = isEmailValid && isPasswordValid
                
                val isEnabled = if (isLoginMode) isLoginValid else isRegistrationValid
                
                Button(
                    onClick = { 
                        if (isLoginMode) {
                            onLogin(email, password)
                        } else {
                            selectedCourseId?.let { courseId ->
                                onRegister(email, password, name, courseId, selectedGoal, avatarFilePath) 
                            }
                        }
                    },
                    enabled = isEnabled && !uiState.isAuthLoading,
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
                    if (uiState.isAuthLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isLoginMode) "Restore Progress" else "Ignite Your Journey",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
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
