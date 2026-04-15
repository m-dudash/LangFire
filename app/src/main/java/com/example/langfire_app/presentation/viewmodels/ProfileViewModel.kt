package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.repository.AppSeeder
import com.example.langfire_app.domain.repository.SyncManager
import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.CourseLevelInfo
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.engine.FortuneWheelMechanic
import com.example.langfire_app.domain.repository.AuthRepository
import com.example.langfire_app.domain.repository.SyncRepository
import com.example.langfire_app.domain.usecase.CreateProfileUseCase
import com.example.langfire_app.domain.usecase.GetAchievementsUseCase
import com.example.langfire_app.domain.usecase.GetAllCoursesUseCase
import com.example.langfire_app.domain.usecase.GetProfileStatsUseCase
import com.example.langfire_app.domain.usecase.GetProfileUseCase
import com.example.langfire_app.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getAllCoursesUseCase: GetAllCoursesUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getProfileStatsUseCase: GetProfileStatsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager,
    private val appSeeder: AppSeeder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkProfile()
    }

    private fun checkProfile() {
        viewModelScope.launch {
            val profile = getProfileUseCase()
            if (profile != null) {
                loadProfileData(profile)
            } else {
                val courses = getAllCoursesUseCase()
                _uiState.update {
                    it.copy(
                        hasProfile = false,
                        isLoading = false,
                        availableCourses = courses
                    )
                }
            }
        }
    }

    private suspend fun loadProfileData(profile: Profile){
        val achievementDeferred = viewModelScope.async {
            getAchievementsUseCase.getAll(profile.id)
        }

        val unlockedAchievementsDeferred = viewModelScope.async {
            getAchievementsUseCase.getUnlocked(profile.id)
        }

        val statsDeferred = viewModelScope.async {
            getProfileStatsUseCase(profile.id)
        }


        val achievements = achievementDeferred.await()
        val unlockedAchievements = unlockedAchievementsDeferred.await()
        val stats = statsDeferred.await()

        val totalAnswers = stats.totalCorrect + stats.totalErrors
        val accuracy = if (totalAnswers > 0)
            stats.totalCorrect.toFloat() / totalAnswers * 100f
        else 0f

        val hasSuperWin = unlockedAchievements.any { it.type == FortuneWheelMechanic.UNIQUE_FORTUNE_ACHIEVEMENT_TYPE }

        _uiState.update {
            it.copy(
                hasProfile = true,
                isLoading = false,
                profile = profile,
                achievements = achievements,
                totalCorrect = stats.totalCorrect,
                totalErrors = stats.totalErrors,
                wordsLearned = stats.wordsLearned,
                toughestWord = stats.toughestWord,
                accuracyPercent = accuracy,
                courseProgress = stats.courseProgress,
                hasSuperWin = hasSuperWin,
                streakFreezes = profile.streakFreezes,
                correctToday = stats.correctToday,
                dailyWordGoal = profile.dailyWordGoal
            )
        }
    }

    fun updateDailyGoal(newGoal: Int) {
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val updatedProfile = currentProfile.copy(dailyWordGoal = newGoal)
            updateProfileUseCase(updatedProfile)
            _uiState.update { it.copy(profile = updatedProfile, dailyWordGoal = newGoal) }
        }
    }
    
    fun onLogin(email: String, password: String) {
        _uiState.update { it.copy(isAuthLoading = true, authError = null) }
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                result.onSuccess {
                    val statsResult = syncRepository.downloadStats()
                    statsResult.onSuccess { stats ->
                        if (stats != null) {
                            try {
                                syncRepository.restoreStats(stats)
                                appSeeder.seedRulesIfMissing()
                                checkProfile()
                                _uiState.update { it.copy(isAuthLoading = false) }
                            } catch (e: Exception) {
                                _uiState.update { it.copy(isAuthLoading = false, authError = "Restore failed: ${e.message}") }
                            }
                        } else {
                            _uiState.update { it.copy(isAuthLoading = false, authError = "Account found, but no cloud progress to restore. Please register as New Pilot or check connection.") }
                        }
                    }
                    statsResult.onFailure { e ->
                        _uiState.update { it.copy(isAuthLoading = false, authError = "Download failed: ${e.message}") }
                    }
                }
                result.onFailure { e ->
                    val msg = e.message ?: ""
                    val errorMsg = when {
                        msg.contains("USER_NOT_FOUND", ignoreCase = true) || msg.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) -> "Account not found. Use 'New Pilot' to register."
                        msg.contains("INVALID_PASSWORD", ignoreCase = true) || msg.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) -> "Wrong password."
                        msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> "Invalid email or password."
                        msg.contains("INVALID_EMAIL", ignoreCase = true) -> "Invalid email format."
                        msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) -> "Too many attempts. Please try again later."
                        msg.contains("NETWORK", ignoreCase = true) -> "Network error. Check your connection."
                        else -> msg.ifEmpty { "Authentication failed" }
                    }
                    _uiState.update { it.copy(isAuthLoading = false, authError = errorMsg) }
                }
            }
        }
    }

    fun onRegister(email: String, password: String, name: String, courseId: Int, dailyGoal: Int, avatarPath: String? = null) {
        _uiState.update { it.copy(isAuthLoading = true, authError = null) }
        viewModelScope.launch {
            authRepository.register(email, password).collect { result ->
                if (result.isSuccess) {
                    try {
                        createProfileUseCase(name, courseId, dailyGoal, avatarPath)
                        
                        viewModelScope.launch {
                            try { syncManager.scheduleSync() } catch (e: Exception) {}
                        }
                        
                        checkProfile()
                        _uiState.update { it.copy(isAuthLoading = false) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isAuthLoading = false, authError = e.message) }
                    }
                } else {
                    _uiState.update { it.copy(isAuthLoading = false, authError = result.exceptionOrNull()?.message) }
                }
            }
        }
    }

    fun onUpdateProfile(name: String, avatarPath: String?) {
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val updatedProfile = currentProfile.copy(name = name, avatarPath = avatarPath)
            updateProfileUseCase(updatedProfile)
            _uiState.update { it.copy(profile = updatedProfile) }
        }
    }
}

data class ProfileUiState(
    val hasProfile: Boolean = false,
    val name: String = "",
    val profile: Profile? = null,
    val availableCourses: List<Course> = emptyList(),
    val isLoading: Boolean = true,
    val achievements: List<Achievement> = emptyList(),
    val totalCorrect: Int = 0,
    val totalErrors: Int = 0,
    val wordsLearned: Int = 0,
    val toughestWord: String? = null,
    val accuracyPercent: Float = 0f,
    val courseProgress: List<CourseLevelInfo> = emptyList(),
    val hasSuperWin: Boolean = false,
    val streakFreezes: Int = 0,
    val correctToday: Int = 0,
    val dailyWordGoal: Int = 0,
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)