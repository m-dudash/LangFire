package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.repository.AchievementRepository
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend fun getAll(profileId: Int): List<Achievement> {
        return achievementRepository.getAchievementsByProfile(profileId)
            .filterNot { shouldHide(it) }
    }

    suspend fun getUnlocked(profileId: Int): List<Achievement> {
        return achievementRepository.getUnlockedAchievements(profileId)
            .filterNot { shouldHide(it) }
    }

    suspend fun getLocked(profileId: Int): List<Achievement> {
        return achievementRepository.getLockedAchievements(profileId)
            .filterNot { shouldHide(it) }
    }

    private fun shouldHide(achievement: Achievement): Boolean {
        return achievement.type == XP_ONLY_ACHIEVEMENT_TYPE ||
            achievement.id in XP_ONLY_ACHIEVEMENT_IDS
    }

    private companion object {
        const val XP_ONLY_ACHIEVEMENT_TYPE = "xp_only"
        val XP_ONLY_ACHIEVEMENT_IDS = setOf(17, 18)
    }
}
