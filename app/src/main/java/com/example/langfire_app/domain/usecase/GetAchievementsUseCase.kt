package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.repository.AchievementRepository
import javax.inject.Inject

/**
 * Use case: Get all achievements for a profile.
 *
 * Used by the profile screen to display medals, trophies, and badges.
 * Can filter by unlocked/locked status.
 */
class GetAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend fun getAll(profileId: Int): List<Achievement> {
        return achievementRepository.getAchievementsByProfile(profileId)
    }

    suspend fun getUnlocked(profileId: Int): List<Achievement> {
        return achievementRepository.getUnlockedAchievements(profileId)
    }

    suspend fun getLocked(profileId: Int): List<Achievement> {
        return achievementRepository.getLockedAchievements(profileId)
    }
}
