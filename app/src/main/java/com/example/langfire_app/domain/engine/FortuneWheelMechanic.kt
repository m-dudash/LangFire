package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Encapsulates all Fortune-Wheel ("koleso šťastia") logic.
 *
 * Extracted from [GamificationEngine] so the engine stays lean and each
 * mechanic is self-contained. The public surface is a single method:
 * [processSpin].
 */
@Singleton
class FortuneWheelMechanic @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun getAvailableRewards(profileId: Int): List<FortuneReward> {
        val hasUnique = achievementRepository
            .getAchievementsByType(profileId, UNIQUE_FORTUNE_ACHIEVEMENT_TYPE)
            .isNotEmpty()

        val baseRewards = listOf(
            FortuneReward.Multiplier(2),
            FortuneReward.Multiplier(3),
            FortuneReward.Multiplier(5),
            FortuneReward.Xp(200),
            FortuneReward.Xp(300),
            FortuneReward.Xp(500)
        )

        return if (hasUnique) baseRewards else baseRewards + FortuneReward.UniqueAchievement
    }

    /**
     * Execute a fortune-wheel spin for the given profile.
     *
     * Business rules:
     * - Only one spin per calendar day is rewarded.
     * - Reward is picked via weighted random from a predefined table.
     * - A unique rare achievement can be won at most once ever.
     *
     * @return [EngineResult] containing the chosen [FortuneReward].
     */
    suspend fun processSpin(profileId: Int): EngineResult {
        val now = System.currentTimeMillis()

        val spinsToday = behaviorRepository.getBehaviorsByTypeAfter(
            profileId     = profileId,
            type          = "fortune_spin",
            fromTimestamp  = startOfToday(now)
        )

        // Already spun today (the current spin is already saved before this call)
        if (spinsToday.size > 1) return EngineResult()

        val hasUnique = achievementRepository
            .getAchievementsByType(profileId, UNIQUE_FORTUNE_ACHIEVEMENT_TYPE)
            .isNotEmpty()

        val options = mutableListOf(
            RewardOption(FortuneReward.Multiplier(2), 20.0),
            RewardOption(FortuneReward.Multiplier(3), 12.0),
            RewardOption(FortuneReward.Multiplier(5), 6.0),
            RewardOption(FortuneReward.Xp(200), 22.0),
            RewardOption(FortuneReward.Xp(300), 12.0),
            RewardOption(FortuneReward.Xp(500), 6.0)
        )

        if (!hasUnique) {
            options.add(RewardOption(FortuneReward.UniqueAchievement, 0.2))
        }

        val reward = pickWeightedReward(options)

        when (reward) {
            is FortuneReward.Xp -> profileRepository.addXp(profileId, reward.amount)
            is FortuneReward.Multiplier -> {
                val expiresAt = now + TimeUnit.HOURS.toMillis(FORTUNE_MULTIPLIER_HOURS)
                profileRepository.clearXpMultiplier(profileId)
                profileRepository.setXpMultiplier(profileId, reward.multiplier, expiresAt)
            }
            FortuneReward.UniqueAchievement -> {
                val achievement = Achievement(
                    type        = UNIQUE_FORTUNE_ACHIEVEMENT_TYPE,
                    unlocked    = true,
                    description = "Unique fortune wheel reward",
                    profileId   = profileId
                )
                achievementRepository.saveAchievement(achievement)
            }
        }

        return EngineResult(
            xpGranted     = if (reward is FortuneReward.Xp) reward.amount else 0,
            fortuneReward = reward
        )
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun startOfToday(now: Long): Long {
        val days = TimeUnit.MILLISECONDS.toDays(now)
        return TimeUnit.DAYS.toMillis(days)
    }

    private fun pickWeightedReward(options: List<RewardOption>): FortuneReward {
        val total = options.sumOf { it.weight }
        val r = Random.nextDouble(total)
        var acc = 0.0
        for (option in options) {
            acc += option.weight
            if (r <= acc) return option.reward
        }
        return options.last().reward
    }

    private data class RewardOption(
        val reward: FortuneReward,
        val weight: Double
    )

    companion object {
        const val UNIQUE_FORTUNE_ACHIEVEMENT_TYPE = "unique_fortune_reward"
        const val FORTUNE_MULTIPLIER_HOURS = 4L
    }
}
