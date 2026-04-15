package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.usecase.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkProfile()
    }

    fun checkProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = getProfileUseCase()
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    hasProfile = profile != null
                ) 
            }
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false
)
