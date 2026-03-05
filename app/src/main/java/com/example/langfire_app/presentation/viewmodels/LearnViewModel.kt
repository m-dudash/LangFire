package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.data.local.dao.SessionWordItem
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

// ─── UI state ────────────────────────────────────────────────────────────────

/** Possible phases of a single flashcard. */
enum class CardPhase { FRONT, REVEALED, ANSWERED }

/** Possible phases of the whole session. */
enum class SessionPhase { LOADING, INTRO, STUDYING, FINISHED, EMPTY }

data class LearnUiState(
    val sessionPhase: SessionPhase = SessionPhase.LOADING,

    // current session queue
    val queue: List<SessionWordItem> = emptyList(),
    val currentIndex: Int = 0,

    // flashcard phase for the current card
    val cardPhase: CardPhase = CardPhase.FRONT,

    // summary counters (updated as the user answers)
    val totalInSession: Int = 0,
    val correctCount: Int = 0,
    val forgotCount: Int = 0
) {
    val currentWord: SessionWordItem? get() = queue.getOrNull(currentIndex)
    val progress: Float get() = if (totalInSession == 0) 0f else currentIndex.toFloat() / totalInSession
    val isLastCard: Boolean get() = currentIndex >= queue.lastIndex
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState = _uiState.asStateFlow()

    init { loadSession() }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Called when the user taps "LET'S BURN IT!" on the intro screen. */
    fun startSession() {
        _uiState.update { it.copy(sessionPhase = SessionPhase.STUDYING, cardPhase = CardPhase.FRONT) }
    }

    /** Flip the card to reveal the translation. */
    fun revealCard() {
        if (_uiState.value.cardPhase == CardPhase.FRONT) {
            _uiState.update { it.copy(cardPhase = CardPhase.REVEALED) }
        }
    }

    /**
     * Record the user's answer and advance to the next card.
     * [quality] is one of [SrsEngine.Quality]: FORGOT / GOOD / EASY
     */
    fun answer(quality: SrsEngine.Quality) {
        val state = _uiState.value
        val word = state.currentWord ?: return
        if (state.cardPhase != CardPhase.REVEALED) return

        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile() ?: return@launch

            // Save SM-2 result to DB
            learnRepository.recordAnswer(profile.id, word.wordId, quality)

            val wasCorrect = quality != SrsEngine.Quality.FORGOT

            // Advance state
            val nextIndex = state.currentIndex + 1
            val isFinished = nextIndex >= state.queue.size

            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    cardPhase = CardPhase.FRONT,
                    correctCount = it.correctCount + if (wasCorrect) 1 else 0,
                    forgotCount = it.forgotCount + if (!wasCorrect) 1 else 0,
                    sessionPhase = if (isFinished) SessionPhase.FINISHED else SessionPhase.STUDYING
                )
            }
        }
    }

    /** Restart a new session (called from the finish screen). */
    fun restart() {
        loadSession()
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { LearnUiState(sessionPhase = SessionPhase.LOADING) }

            val profile  = profileRepository.getActiveProfile()
            val courseId = settingsRepository.getCurrentCourseId()

            if (profile == null || courseId == null) {
                _uiState.update { it.copy(sessionPhase = SessionPhase.EMPTY) }
                return@launch
            }

            val words = learnRepository.getSessionWords(profile.id, courseId, limit = 20)

            if (words.isEmpty()) {
                _uiState.update { it.copy(sessionPhase = SessionPhase.EMPTY) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    sessionPhase   = SessionPhase.INTRO,
                    queue          = words,
                    currentIndex   = 0,
                    cardPhase      = CardPhase.FRONT,
                    totalInSession = words.size,
                    correctCount   = 0,
                    forgotCount    = 0
                )
            }
        }
    }
}
