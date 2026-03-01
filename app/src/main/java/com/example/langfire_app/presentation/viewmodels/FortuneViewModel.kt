package com.example.langfire_app.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.FortuneReward
import com.example.langfire_app.domain.usecase.GetProfileUseCase
import com.example.langfire_app.domain.usecase.ProcessBehaviorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FortuneViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val processBehaviorUseCase: ProcessBehaviorUseCase,
    private val behaviorRepository: com.example.langfire_app.domain.repository.BehaviorRepository
) : ViewModel() {

    private val _isAlreadySpun = MutableStateFlow(false)
    val isAlreadySpun: StateFlow<Boolean> = _isAlreadySpun.asStateFlow()

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        viewModelScope.launch {
            val profile = getProfileUseCase() ?: return@launch
            val now = System.currentTimeMillis()
            val startOfToday = (now / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
            val spins = behaviorRepository.getBehaviorsByTypeAfter(profile.id, "fortune_spin", startOfToday)
            if (spins.isNotEmpty()) {
                _isAlreadySpun.value = true
            }
        }
    }

    suspend fun spin(): FortuneReward? {
        val profile = getProfileUseCase() ?: return null
        val result = processBehaviorUseCase(
            Behavior(type = "fortune_spin", profileId = profile.id)
        )
        return result.fortuneReward
    }
}