package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.data.local.dao.StatWordItem
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
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.usecase.ProcessBehaviorUseCase

enum class StatCategory { TO_LEARN, PRACTICED, LEARNED }


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val courseRepository: CourseRepository,
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
    private val processBehaviorUseCase: ProcessBehaviorUseCase,
    private val behaviorRepository: com.example.langfire_app.domain.repository.BehaviorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /** Call this to force a full data reload, e.g. when returning from FortuneWheel. */
    fun refresh() {
        refreshSilently()
    }

    /**
     * Re-fetches only the fields that can change during a session
     * (XP, multiplier, fortune availability, course stats) WITHOUT
     * touching [isLoading], so the UI never flashes a spinner on resume.
     */
    private fun refreshSilently() {
        viewModelScope.launch {
            val profile = getProfileUseCase.invoke() ?: return@launch
            _uiState.update {
                it.copy(
                    xp                    = profile.xp,
                    xpMultiplier          = profile.xpMultiplier,
                    xpMultiplierExpiresAt = profile.xpMultiplierExpiresAt,
                    streakDays            = profile.streakDays,
                )
            }
            checkFortuneAvailability(profile.id)

            val courseId = _uiState.value.activeCourseId ?: return@launch
            loadHomeCourseStats(profile.id, courseId)
        }
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
                        xpMultiplier = profile.xpMultiplier,
                        xpMultiplierExpiresAt = profile.xpMultiplierExpiresAt,
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
                checkFortuneAvailability(profile.id)
            }
        }
    }

    private suspend fun checkFortuneAvailability(profileId: Int) {
        val now = System.currentTimeMillis()
        val startOfToday = (now / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
        val spins = behaviorRepository.getBehaviorsByTypeAfter(profileId, "fortune_spin", startOfToday)
        _uiState.update { it.copy(isFortuneWheelAvailable = spins.isEmpty()) }
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

    fun spinFortuneWheel() {
        viewModelScope.launch {
            val profile = getProfileUseCase() ?: return@launch
            val behavior = Behavior(
                type = "fortune_spin",
                profileId = profile.id
            )
            processBehaviorUseCase(behavior)

            val updated = getProfileUseCase()
            if (updated != null) {
                _uiState.update {
                    it.copy(
                        xp = updated.xp,
                        xpMultiplier = updated.xpMultiplier,
                        xpMultiplierExpiresAt = updated.xpMultiplierExpiresAt,
                    )
                }
            }
        }
    }

    /** Open the stats bottom sheet for the given category and load its words. */
    fun showStatSheet(category: StatCategory) {
        viewModelScope.launch {
            val profile = getProfileUseCase() ?: return@launch
            val courseId = _uiState.value.activeCourseId ?: return@launch

            _uiState.update {
                it.copy(
                    activeStatCategory = category,
                    isStatSheetLoading = true,
                    statSheetWords = emptyList()
                )
            }

            val words = when (category) {
                StatCategory.TO_LEARN  -> statsRepository.getToLearnWords(profile.id, courseId)
                StatCategory.PRACTICED -> statsRepository.getPracticedWords(profile.id, courseId)
                StatCategory.LEARNED   -> statsRepository.getLearnedWords(profile.id, courseId)
            }

            _uiState.update {
                it.copy(
                    isStatSheetLoading = false,
                    statSheetWords = words
                )
            }
        }
    }

    fun hideStatSheet() {
        _uiState.update { it.copy(activeStatCategory = null, statSheetWords = emptyList()) }
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
    val xpMultiplier: Int = 1,
    val xpMultiplierExpiresAt: Long? = null,
    val isFortuneWheelAvailable: Boolean = true,
    // Stats Bottom Sheet
    val activeStatCategory: StatCategory? = null,
    val statSheetWords: List<StatWordItem> = emptyList(),
    val isStatSheetLoading: Boolean = false,
)
