package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.repository.StatsRepository
import com.example.langfire_app.domain.usecase.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository
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
                        streakDays = profile.streakDays,
                        xp = profile.xp,
                    )
                }
            } else {
                _uiState.update { it.copy(hasActiveProfile = false) }
            }

            // ── Load courses ──────────────────────────────────────────────
            val allCourses   = courseRepository.getAllCourses()
            val savedCourseId = settingsRepository.getCurrentCourseId()

            val activeCourse: Course? = when {
                savedCourseId != null ->
                    courseRepository.getCourseById(savedCourseId)
                        ?: allCourses.firstOrNull()
                allCourses.isNotEmpty() -> {
                    val first = allCourses.first()
                    settingsRepository.setCurrentCourseId(first.id)
                    first
                }
                else -> null
            }

            _uiState.update {
                it.copy(
                    isLoading        = false,
                    availableCourses = allCourses,
                    activeCourseId   = activeCourse?.id,
                    languageName     = activeCourse?.name     ?: it.languageName,
                    languageFlag     = activeCourse?.icon     ?: it.languageFlag,
                    languageLevel    = it.languageLevel  // kept; can be wired to StatsRepo later
                )
            }

            if (profile != null && activeCourse != null) {
                loadHomeCourseStats(profile.id, activeCourse.id)
            }
        }
    }

    /** Persist the selected course and update the top-bar immediately. */
    fun selectCourse(courseId: Int) {
        viewModelScope.launch {
            settingsRepository.setCurrentCourseId(courseId)
            val course = courseRepository.getCourseById(courseId)
            _uiState.update {
                it.copy(
                    activeCourseId      = courseId,
                    languageName        = course?.name ?: it.languageName,
                    languageFlag        = course?.icon ?: it.languageFlag,
                    showLanguagePicker  = false
                )
            }

            val profile = getProfileUseCase.invoke()
            if (profile != null) {
                loadHomeCourseStats(profile.id, courseId)
            }
        }
    }

    fun showLanguagePicker() { _uiState.update { it.copy(showLanguagePicker = true)  } }
    fun hideLanguagePicker() { _uiState.update { it.copy(showLanguagePicker = false) } }

    fun onBurnClick() {
        // Logic for burning/streak
    }

    private suspend fun loadHomeCourseStats(profileId: Int,courseId: Int){
        val stats = statsRepository.getHomeCourseStats(profileId, courseId)
        _uiState.update {
            it.copy(
                toLearnCount = stats.toLearn,
                practicedCount = stats.practiced,
                learnedCount = stats.learned
            )
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasActiveProfile: Boolean = false,
    val streakDays: Int = 1,
    val xp: Int = 0,
    val learnedWords: Int = 0,
    val learnedWordsGoal: Int = 100,
    val languageName: String = "Language",
    val languageFlag: String = "🏳️",
    val languageLevel: String = "A1",
    val availableCourses: List<Course> = emptyList(),
    val activeCourseId: Int? = null,
    val showLanguagePicker: Boolean = false,
    val toLearnCount: Int = 0,
    val practicedCount: Int = 0,
    val learnedCount: Int = 0,
)
