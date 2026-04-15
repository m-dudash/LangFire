package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.model.UnitWordItem
import com.example.langfire_app.domain.usecase.ClearUnitWordUseCase
import com.example.langfire_app.domain.usecase.GetUnitDetailsUseCase
import com.example.langfire_app.domain.usecase.MarkUnitWordUseCase
import com.example.langfire_app.domain.usecase.MarkWordResult
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
    val words: List<UnitWordItem> = emptyList()
)

@HiltViewModel
class UnitDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUnitDetailsUseCase: GetUnitDetailsUseCase,
    private val markUnitWordUseCase: MarkUnitWordUseCase,
    private val clearUnitWordUseCase: ClearUnitWordUseCase
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
            val result = getUnitDetailsUseCase(unitId)
            if (result != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        unitName = result.unitName,
                        words = result.words
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun markWord(wordId: Int, coeff: Float) {
        viewModelScope.launch {
            val result = markUnitWordUseCase(wordId, coeff)
            if (result is MarkWordResult.QueueFull) {
                _errorEvent.emit(result.message)
                return@launch
            }
            refreshWords()
        }
    }

    fun clearWord(wordId: Int) {
        viewModelScope.launch {
            clearUnitWordUseCase(wordId)
            refreshWords()
        }
    }

    private suspend fun refreshWords() {
        val result = getUnitDetailsUseCase(unitId) ?: return
        _uiState.update { it.copy(words = result.words) }
    }
}
