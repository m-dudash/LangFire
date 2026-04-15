package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.engine.GamificationEngine
import javax.inject.Inject

class GetCurrentStreakUseCase @Inject constructor(
    private val gamificationEngine: GamificationEngine
) {
    suspend operator fun invoke(profileId: Int): Int {
        return gamificationEngine.getCurrentStreak(profileId)
    }
}
