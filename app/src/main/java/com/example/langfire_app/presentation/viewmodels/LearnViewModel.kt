package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.data.local.dao.SessionWordItem
import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.EngineResult
import com.example.langfire_app.domain.model.SessionBehaviorBuilder
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.domain.repository.LearnRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// ─── Exercise Types ──────────────────────────────────────────────────────────

enum class ExerciseType {
    FLASHCARD_L1,        // Native -> Target (Recall)
    FLASHCARD_L2,        // Target -> Native (Reading)
    TRANSLATE_L1,        // Target -> Native (multiple choice)
    TRANSLATE_L2,        // Native -> Target (multiple choice)
    LISTENING_CHOOSE,    // Audio only -> Translation (multiple choice)
    LISTENING_TYPE,      // Audio only -> Text input
    LISTENING_FLIP,      // Audio only -> Flip card to reveal and grade
    MATCHING             // 4 pairs matching
}

data class ExerciseItem(
    val word: SessionWordItem,
    val type: ExerciseType,
    // Multiple choice / Matching
    val options: List<SessionWordItem> = emptyList() // The correct word is mixed in
)

// ─── UI statet ───────────────────────────────────────────────────────────────

enum class CardPhase { FRONT, REVEALED, ANSWERED }
enum class SessionPhase { LOADING, INTRO, STUDYING, FINISHED, EMPTY }

data class LearnUiState(
    val sessionPhase: SessionPhase = SessionPhase.LOADING,

    // current session queue
    val queue: List<ExerciseItem> = emptyList(),
    val currentIndex: Int = 0,

    // flashcard/exercise phase for the current card
    val cardPhase: CardPhase = CardPhase.FRONT,
    // For text input
    val typedText: String = "",
    val isTypeCorrect: Boolean? = null,
    val selectedWordId: Int? = null,

    // summary counters
    val totalInSession: Int = 0,
    val correctCount: Int = 0,
    val forgotCount: Int = 0,

    // gamification result (populated when session finishes)
    val engineResult: EngineResult? = null
) {
    val currentExercise: ExerciseItem? get() = queue.getOrNull(currentIndex)
    val progress: Float get() = if (totalInSession == 0) 0f else (currentIndex + 1).toFloat() / totalInSession
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val gamificationEngine: GamificationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState = _uiState.asStateFlow()

    /** Timestamp when the user started the current session (used for session_time). */
    private var sessionStartedAt: Long = 0L

    init { loadSession() }

    fun startSession() {
        sessionStartedAt = System.currentTimeMillis()
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
        if (_uiState.value.cardPhase != CardPhase.FRONT) return // prevent double click
        
        val isCorrect = selectedWordId == exercise.word.wordId
        val quality = if (isCorrect) SrsEngine.Quality.GOOD else SrsEngine.Quality.FORGOT
        
        // Show result for a moment
        _uiState.update { it.copy(cardPhase = CardPhase.REVEALED, selectedWordId = selectedWordId) }
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
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
        val state = _uiState.value
        val word = state.currentExercise?.word ?: return

        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile() ?: return@launch

            // Save SM-2 result to DB
            learnRepository.recordAnswer(profile.id, word.wordId, quality)

            val wasCorrect = quality != SrsEngine.Quality.FORGOT

            val nextIndex = state.currentIndex + 1
            val isFinished = nextIndex >= state.queue.size

            // Calculate final counts BEFORE updating state
            val finalCorrect = state.correctCount + if (wasCorrect) 1 else 0
            val finalForgot  = state.forgotCount  + if (!wasCorrect) 1 else 0

            // If session is finished, process gamification
            var engineResult: EngineResult? = null
            if (isFinished) {
                val elapsedSec = (System.currentTimeMillis() - sessionStartedAt) / 1000
                val behavior = SessionBehaviorBuilder.build(
                    profileId          = profile.id,
                    correctCount       = finalCorrect,
                    forgotCount        = finalForgot,
                    totalExercises     = state.totalInSession,
                    sessionDurationSec = elapsedSec
                )
                engineResult = gamificationEngine.processBehavior(behavior)
            }

            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    cardPhase = CardPhase.FRONT,
                    typedText = "",
                    isTypeCorrect = null,
                    selectedWordId = null,
                    correctCount = finalCorrect,
                    forgotCount = finalForgot,
                    sessionPhase = if (isFinished) SessionPhase.FINISHED else SessionPhase.STUDYING,
                    engineResult = engineResult
                )
            }
        }
    }

    // For matching, we assume they did it correctly if they used the UI
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

            val words = learnRepository.getSessionWords(profile.id, courseId, limit = 15)
            if (words.isEmpty()) {
                _uiState.update { it.copy(sessionPhase = SessionPhase.EMPTY) }
                return@launch
            }

            // Generate an exercise mix
            val exercises = mutableListOf<ExerciseItem>()
            
            // For matching, we need a group of 4 words. If we have less than 4, we skip matching.
            var matchingCandidateGroup: List<SessionWordItem>? = null
            if (words.size >= 4) {
                matchingCandidateGroup = words.take(4)
            }

            for (word in words) {
                // Determine exercise type. Based on repetition (how well they know it).
                // New word (r=0) -> Flashcard
                // Low rep (r=1) -> Translate L2 (Easier)
                // Mid rep (r=2) -> Translate L1
                // High rep (r>=3) -> Listening or Typing or Matching
                
                var type = ExerciseType.FLASHCARD_L2
                val rep = listOf(1,0).random()
                
                // Mix in matching if we are looking at one of the first 4 words and we decided to do matching
                if (matchingCandidateGroup != null && matchingCandidateGroup.contains(word)) {
                    // We only emit the MATCHING exercise once for the group
                    if (word == matchingCandidateGroup.first()) {
                        exercises.add(ExerciseItem(word, ExerciseType.MATCHING, options = matchingCandidateGroup))
                    }
                    continue // Skip adding an individual exercise for words inside a matching group
                }

                type = when {
                    rep == 0 -> listOf(ExerciseType.FLASHCARD_L1, ExerciseType.FLASHCARD_L2, ExerciseType.LISTENING_FLIP, ExerciseType.TRANSLATE_L2, ExerciseType.LISTENING_TYPE).random()
                    rep == 1 -> listOf(ExerciseType.TRANSLATE_L1, ExerciseType.TRANSLATE_L2, ExerciseType.LISTENING_CHOOSE, ExerciseType.LISTENING_TYPE, ExerciseType.FLASHCARD_L1).random()
                    else -> listOf(ExerciseType.LISTENING_TYPE, ExerciseType.LISTENING_CHOOSE, ExerciseType.LISTENING_FLIP, ExerciseType.TRANSLATE_L1, ExerciseType.FLASHCARD_L1).random()
                }

                // If it's a multiple choice, load distractors
                var options = emptyList<SessionWordItem>()
                if (type == ExerciseType.TRANSLATE_L1 || type == ExerciseType.TRANSLATE_L2 || type == ExerciseType.LISTENING_CHOOSE) {
                    val distractors = learnRepository.getDistractors(profile.id, courseId, word.wordId, limit = 3)
                    options = (distractors + word).shuffled()
                    // Fallback to flashcard if not enough words in DB
                    if (options.size < 4) {
                        type = listOf(ExerciseType.FLASHCARD_L1, ExerciseType.FLASHCARD_L2).random()
                    }
                }

                exercises.add(ExerciseItem(word, type, options))
            }

            _uiState.update {
                it.copy(
                    sessionPhase   = SessionPhase.INTRO,
                    queue          = exercises.shuffled(Random(System.currentTimeMillis())),
                    currentIndex   = 0,
                    cardPhase      = CardPhase.FRONT,
                    totalInSession = exercises.size,
                    correctCount   = 0,
                    forgotCount    = 0,
                    engineResult   = null
                )
            }
        }
    }
}
