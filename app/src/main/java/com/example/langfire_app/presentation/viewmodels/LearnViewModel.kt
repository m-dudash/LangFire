package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.model.SessionWord
import com.example.langfire_app.domain.repository.SyncManager
import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.EngineResult
import com.example.langfire_app.domain.model.SessionBehaviorBuilder
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.domain.repository.LearnRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.repository.SyncRepository
import com.example.langfire_app.domain.usecase.GetProfileStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

enum class ExerciseType {
    FLASHCARD_L1,
    FLASHCARD_L2,
    TRANSLATE_L1,
    TRANSLATE_L2,
    LISTENING_CHOOSE,
    LISTENING_TYPE,
    LISTENING_FLIP,
    MATCHING
}

data class ExerciseItem(
    val word: SessionWord,
    val type: ExerciseType,
    val options: List<SessionWord> = emptyList()
)


enum class CardPhase { FRONT, REVEALED, ANSWERED }
enum class SessionPhase { LOADING, INTRO, STUDYING, FINISHED, EMPTY }

data class LearnUiState(
    val sessionPhase: SessionPhase = SessionPhase.LOADING,
    val queue: List<ExerciseItem> = emptyList(),
    val currentIndex: Int = 0,
    val cardPhase: CardPhase = CardPhase.FRONT,
    val typedText: String = "",
    val isTypeCorrect: Boolean? = null,
    val selectedWordId: Int? = null,
    val totalInSession: Int = 0,
    val correctCount: Int = 0,
    val forgotCount: Int = 0,

    val engineResult: EngineResult? = null,
    val correctToday: Int = 0,
    val dailyGoal: Int = 0
) {
    val currentExercise: ExerciseItem? get() = queue.getOrNull(currentIndex)
    val progress: Float get() = if (totalInSession == 0) 0f else (currentIndex + 1).toFloat() / totalInSession
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val gamificationEngine: GamificationEngine,
    private val getProfileStatsUseCase: GetProfileStatsUseCase,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState = _uiState.asStateFlow()

    private var sessionStartedAt: Long = 0L

    private val pendingAnswers = mutableListOf<Pair<Int, SrsEngine.Quality>>()
    private var pendingCorrectAnswers = 0
    private var isProcessingAnswer = false

    init { loadSession() }

    fun startSession() {
        sessionStartedAt = System.currentTimeMillis()
        pendingAnswers.clear()
        pendingCorrectAnswers = 0
        isProcessingAnswer = false
        _uiState.update { it.copy(sessionPhase = SessionPhase.STUDYING, cardPhase = CardPhase.FRONT) }
    }

    fun revealCard() {
        if (_uiState.value.cardPhase == CardPhase.FRONT) {
            _uiState.update { it.copy(cardPhase = CardPhase.REVEALED) }
        }
    }

    fun setTypedText(text: String) {
        _uiState.update { it.copy(typedText = text) }
    }

    fun answerMultipleChoice(selectedWordId: Int) {
        val exercise = _uiState.value.currentExercise ?: return
        if (_uiState.value.cardPhase != CardPhase.FRONT) return

        val isCorrect = selectedWordId == exercise.word.wordId
        val quality = if (isCorrect) SrsEngine.Quality.GOOD else SrsEngine.Quality.FORGOT

        _uiState.update { it.copy(cardPhase = CardPhase.REVEALED, selectedWordId = selectedWordId) }

        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            if (_uiState.value.cardPhase == CardPhase.REVEALED && _uiState.value.selectedWordId == selectedWordId) {
                answer(quality)
            }
        }
    }

    fun skipDelay() {
        if (_uiState.value.cardPhase == CardPhase.REVEALED && _uiState.value.selectedWordId != null) {
            val exercise = _uiState.value.currentExercise ?: return
            val isCorrect = _uiState.value.selectedWordId == exercise.word.wordId
            val quality = if (isCorrect) SrsEngine.Quality.GOOD else SrsEngine.Quality.FORGOT
            answer(quality)
        }
    }

    fun answerTypeInput() {
        val state = _uiState.value
        val exercise = state.currentExercise ?: return

        val actual = state.typedText.trim().lowercase().replace(Regex("\\p{Punct}"), "")
        val expected = exercise.word.word.trim().lowercase().replace(Regex("\\p{Punct}"), "")

        val isCorrect = (actual == expected && actual.isNotEmpty())
        _uiState.update { it.copy(cardPhase = CardPhase.REVEALED, isTypeCorrect = isCorrect) }
    }

    fun proceedTypeInput() {
        val isCorrect = _uiState.value.isTypeCorrect == true
        val quality = if (isCorrect) SrsEngine.Quality.GOOD else SrsEngine.Quality.FORGOT
        answer(quality)
    }

    fun answer(quality: SrsEngine.Quality) {
        if (isProcessingAnswer) return
        isProcessingAnswer = true

        val state = _uiState.value
        val exercise = state.currentExercise
        if (exercise == null) {
            isProcessingAnswer = false
            return
        }
        val word = exercise.word

        pendingAnswers.add(Pair(word.wordId, quality))

        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile()
            if (profile == null) {
                isProcessingAnswer = false
                return@launch
            }

            val wasCorrect = quality != SrsEngine.Quality.FORGOT

            val nextIndex = state.currentIndex + 1
            val isFinished = nextIndex >= state.queue.size

            val newlyCorrect = if (wasCorrect) 1 else 0
            if (wasCorrect) {
                pendingCorrectAnswers++
            }
            val totalCorrectToday = state.correctToday + newlyCorrect
            val finalCorrect = state.correctCount + newlyCorrect
            val finalForgot  = state.forgotCount  + if (!wasCorrect) 1 else 0

            var engineResult: EngineResult? = null
            if (isFinished) {
                pendingAnswers.forEach { (wordId, q) ->
                    learnRepository.recordAnswer(profile.id, wordId, q)
                }

                for (i in 0 until pendingCorrectAnswers) {
                    gamificationEngine.processBehavior(
                        com.example.langfire_app.domain.model.Behavior(
                            type = "correct_answer",
                            profileId = profile.id
                        )
                    )
                }

                val elapsedSec = (System.currentTimeMillis() - sessionStartedAt) / 1000
                val behavior = SessionBehaviorBuilder.build(
                    profileId          = profile.id,
                    correctCount       = finalCorrect,
                    forgotCount        = finalForgot,
                    totalExercises     = state.totalInSession,
                    sessionDurationSec = elapsedSec
                )
                engineResult = gamificationEngine.processBehavior(behavior)

                viewModelScope.launch {
                    try { syncManager.scheduleSync() } catch (_: Exception) {}
                }

            }

            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    cardPhase = CardPhase.FRONT,
                    typedText = "",
                    isTypeCorrect = null,
                    selectedWordId = null,
                    sessionPhase = if (isFinished) SessionPhase.FINISHED else SessionPhase.STUDYING,
                    engineResult = engineResult,
                    correctToday = totalCorrectToday,
                    correctCount = finalCorrect,
                    forgotCount = finalForgot
                )
            }
            isProcessingAnswer = false
        }
    }

    fun answerMatchingCompleted(errorsMade: Int) {
        val quality = if (errorsMade == 0) SrsEngine.Quality.GOOD else SrsEngine.Quality.FORGOT
        answer(quality)
    }

    fun restart() {
        loadSession()
    }

    fun loadSession() {
        viewModelScope.launch {
            _uiState.update { LearnUiState(sessionPhase = SessionPhase.LOADING) }

            val profile = profileRepository.getActiveProfile()
            val courseId = settingsRepository.getCurrentCourseId()
            if (profile == null || courseId == null) {
                _uiState.update { it.copy(sessionPhase = SessionPhase.EMPTY) }
                return@launch
            }

            val maxLevelId = learnRepository.getMaxLevelIdForSession(profile.id, courseId)
            val words = if (maxLevelId != null) {
                learnRepository.getSessionWordsBounded(profile.id, courseId, maxLevelId, limit = 50)
            } else {
                learnRepository.getSessionWords(profile.id, courseId, limit = 50)
            }
            if (words.size < 15) {
                _uiState.update { it.copy(sessionPhase = SessionPhase.EMPTY) }
                return@launch
            }

            val exercises = mutableListOf<ExerciseItem>()

            var matchingCandidateGroup: List<SessionWord>? = null
            if (words.size >= 4) {
                matchingCandidateGroup = words.take(4)
            }
            val allDistractors = learnRepository.getDistractors(profile.id, courseId, excludedWordId = -1, limit = 200)
            for (word in words) {
                var type = ExerciseType.FLASHCARD_L2
                val knowledge = word.knowledgeCoeff ?: 0f
                val rep = when {
                    knowledge >= 0.6f -> 3
                    knowledge >= 0.4f -> 2
                    knowledge >= 0.2f -> 1
                    else -> 0
                }
                if (matchingCandidateGroup != null && matchingCandidateGroup.contains(word)) {
                    if (word == matchingCandidateGroup.first()) {
                        exercises.add(ExerciseItem(word, ExerciseType.MATCHING,
                            options = matchingCandidateGroup))
                    }
                    continue
                }
                type = when (rep) {
                    0 -> listOf(ExerciseType.FLASHCARD_L2, ExerciseType.TRANSLATE_L1).random()
                    1 -> listOf(ExerciseType.FLASHCARD_L1, ExerciseType.TRANSLATE_L2,
                        ExerciseType.LISTENING_CHOOSE).random()
                    2 -> listOf(ExerciseType.FLASHCARD_L1, ExerciseType.TRANSLATE_L2,
                        ExerciseType.LISTENING_FLIP, ExerciseType.LISTENING_CHOOSE).random()
                    else -> listOf(ExerciseType.LISTENING_TYPE, ExerciseType.LISTENING_FLIP,
                        ExerciseType.FLASHCARD_L1).random()
                }
                var options = emptyList<SessionWord>()
                if (type == ExerciseType.TRANSLATE_L1 || type == ExerciseType.TRANSLATE_L2 || type == ExerciseType.LISTENING_CHOOSE) {
                    val distractors = allDistractors.filter { it.wordId != word.wordId }.shuffled().take(3)
                    options = (distractors + word).shuffled()
                    if (options.size < 4) {
                        type = listOf(ExerciseType.FLASHCARD_L1, ExerciseType.FLASHCARD_L2).random()
                    }
                }

                exercises.add(ExerciseItem(word, type, options))
            }

            _uiState.update {
                val mixedExercises = exercises.shuffled(Random(System.currentTimeMillis())).take(20)
                it.copy(
                    sessionPhase   = SessionPhase.INTRO,
                    queue          = mixedExercises,
                    currentIndex   = 0,
                    cardPhase      = CardPhase.FRONT,
                    totalInSession = mixedExercises.size,
                    correctCount   = 0,
                    forgotCount    = 0,
                    engineResult   = null,
                    correctToday   = getProfileStatsUseCase(profile.id).correctToday,
                    dailyGoal      = profile.dailyWordGoal
                )
            }
        }
    }
}
