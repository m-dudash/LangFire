package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.usecase.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val courseRepository: CourseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val profile = getProfileUseCase.invoke()

            if (profile != null) {
                _uiState.update {
                    it.copy(
                        hasActiveProfile = true,
                        isLoading = false,
                        streakDays = profile.streakDays,
                        xp = profile.xp,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasActiveProfile = false
                    )
                }
            }


            val savedCourseId = settingsRepository.getCurrentCourseId()

            val activeCourse = if (savedCourseId != null) {
                courseRepository.getCourseById(savedCourseId)
                    ?: courseRepository.getAllCourses().firstOrNull()
            } else {
                val firstCourse = courseRepository.getAllCourses().firstOrNull()
                firstCourse?.let { settingsRepository.setCurrentCourseId(it.id) }
                firstCourse
            }
        }
    }

    fun onBurnClick() {
        // Logic for burning/streak
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val hasActiveProfile: Boolean = false,
    val streakDays: Int = 1,
    val xp: Int = 0,
    val learnedWords: Int = 0,
    val learnedWordsGoal: Int = 100,
    val languageName: String = "Language",
    val languageFlag: String = "🏳️",
    val languageLevel: String = "A1"
)
