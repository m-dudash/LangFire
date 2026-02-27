package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.engine.GamificationEngine
import javax.inject.Inject

/**
 * Use case: Get the current daily streak for a profile.
 *
 * Used by the Presentation Layer to display the fire streak
 * indicator on the home screen.
 */
class GetCurrentStreakUseCase @Inject constructor(
    private val gamificationEngine: GamificationEngine
) {
    suspend operator fun invoke(profileId: Int): Int {
        return gamificationEngine.getCurrentStreak(profileId)
    }
}
