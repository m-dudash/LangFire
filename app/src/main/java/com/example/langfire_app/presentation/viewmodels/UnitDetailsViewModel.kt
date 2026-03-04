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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val unitId: Int = checkNotNull(savedStateHandle["unitId"])

    private val _uiState = MutableStateFlow(UnitDetailsUiState())
    val uiState = _uiState.asStateFlow()

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
            val existing = wordProgressDao.getByWord(profile.id, wordId)
            val updated = existing?.copy(
                knowledgeCoeff = coeff,
                lastReviewed = System.currentTimeMillis()
            ) ?: WordProgressEntity(
                id = 0, // Auto-generate
                knowledgeCoeff = coeff,
                lastReviewed = System.currentTimeMillis(),
                correctCount = if (coeff == 1f) 1 else 0,
                incorrectCount = if (coeff == 0f) 1 else 0,
                profileId = profile.id,
                wordId = wordId
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
