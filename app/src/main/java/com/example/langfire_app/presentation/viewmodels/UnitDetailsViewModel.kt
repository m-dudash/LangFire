package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.data.local.dao.UnitDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.dao.WordsDao
import com.example.langfire_app.data.local.dao.WordWithTranslationAndProgress
import com.example.langfire_app.data.local.entities.WordProgressEntity
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UnitDetailsUiState(
    val isLoading: Boolean = true,
    val unitName: String = "",
    val words: List<WordWithTranslationAndProgress> = emptyList()
)

@HiltViewModel
class UnitDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val wordsDao: WordsDao,
    private val unitDao: UnitDao,
    private val wordProgressDao: WordProgressDao,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val unitId: Int = checkNotNull(savedStateHandle["unitId"])

    private val _uiState = MutableStateFlow(UnitDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = profileRepository.getActiveProfile() ?: return@launch
            val unit = unitDao.getUnitById(unitId)
            val wordsList = wordsDao.getWordsForUnit(unitId, profile.id)
                .sortedBy { it.knowledgeCoeff != null }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    unitName = unit?.name ?: "Unit Details",
                    words = wordsList
                )
            }
        }
    }

    fun markWord(wordId: Int, coeff: Float) {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile() ?: return@launch
            
            if (coeff == 0.0f) {
                val courseId = settingsRepository.getCurrentCourseId() ?: return@launch
                val toLearnCount = wordProgressDao.countToLearnByCourse(profile.id, courseId)
                if (toLearnCount >= 50) {
                    _errorEvent.emit("You already have 50 words in your To Learn queue. Complete a session before adding more!")
                    return@launch
                }
            }
            
            val existing = wordProgressDao.getByWord(profile.id, wordId)
            val updated = existing?.copy(
                knowledgeCoeff = coeff,
                lastReviewed = System.currentTimeMillis(),
                srsRepetition = if (coeff == 1f) -1 else (existing.srsRepetition.takeIf { coeff != 0f } ?: 0),
                srsInterval = if (coeff == 1f) 0 else (existing.srsInterval.takeIf { coeff != 0f } ?: 0),
                nextReviewAt = if (coeff == 1f) 0L else (existing.nextReviewAt.takeIf { coeff != 0f } ?: 0L)
            ) ?: WordProgressEntity(
                id = 0, // Auto-generate
                knowledgeCoeff = coeff,
                lastReviewed = System.currentTimeMillis(),
                correctCount = if (coeff == 1f) 1 else 0,
                incorrectCount = if (coeff == 0f) 1 else 0,
                profileId = profile.id,
                wordId = wordId,
                srsRepetition = if (coeff == 1f) -1 else 0,
                srsInterval = 0,
                nextReviewAt = 0L
            )
            
            if (existing == null) {
                wordProgressDao.insert(updated)
            } else {
                wordProgressDao.update(updated)
            }
            
            // Refresh list to show UI change immediately
            val wordsList = wordsDao.getWordsForUnit(unitId, profile.id)
                .sortedBy { it.knowledgeCoeff != null }
            _uiState.update { it.copy(words = wordsList) }
        }
    }

    fun clearWord(wordId: Int) {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile() ?: return@launch
            val existing = wordProgressDao.getByWord(profile.id, wordId)
            if (existing != null) {
                wordProgressDao.deleteById(existing.id)
            }
            // Refresh list
            val wordsList = wordsDao.getWordsForUnit(unitId, profile.id)
                .sortedBy { it.knowledgeCoeff != null }
            _uiState.update { it.copy(words = wordsList) }
        }
    }
}
