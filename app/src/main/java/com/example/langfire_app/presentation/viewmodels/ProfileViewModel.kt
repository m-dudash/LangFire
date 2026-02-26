package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.usecase.CreateProfileUseCase
import com.example.langfire_app.domain.usecase.GetAchievementsUseCase
import com.example.langfire_app.domain.usecase.GetAllCoursesUseCase
import com.example.langfire_app.domain.usecase.GetCurrentStreakUseCase
import com.example.langfire_app.domain.usecase.GetProfileUseCase
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
    private val getCurrentStreakUseCase: GetCurrentStreakUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getProfileStatsUseCase: GetProfileStatsUseCase
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

        val streakDeferred = viewModelScope.async {
            getCurrentStreakUseCase(profile.id)
        }

        val achievements = achievementDeferred.await()
        val unlockedAchievements = unlockedAchievementsDeferred.await()
        val stats = statsDeferred.await()
        val streak = streakDeferred.await()

        val totalAnswers = stats.totalCorrect + stats.totalErrors
        val accuracy = if (totalAnswers > 0)
            stats.totalCorrect.toFloat() / totalAnswers * 100f
        else 0f


        _uiState.update {
            it.copy(
                hasProfile = true,
                isLoading = false,
                profile = profile.copy(streakDays = streak),
                achievements = achievements,
                totalCorrect = stats.totalCorrect,
                totalErrors = stats.totalErrors,
                wordsLearned = stats.wordsLearned,
                toughestWord = stats.toughestWord,
                accuracyPercent = accuracy,
                courseProgress = stats.courseProgress
            )
        }
    }


    fun onRegister(name: String, courseId: Int) {
        viewModelScope.launch {
            createProfileUseCase(name, courseId)
            checkProfile()
        }
    }
}

data class ProfileUiState(
    val hasProfile: Boolean = false,
    val name: String = "",
    val profile: Profile? = null,
    val availableCourses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val achievements: List<Achievement> = emptyList(),
    val totalCorrect: Int = 0,
    val totalErrors: Int = 0,
    val wordsLearned: Int = 0,
    val toughestWord: String? = null,
    val accuracyPercent: Float = 0f,
    val courseProgress: List<CourseLevelInfo> = emptyList()
)