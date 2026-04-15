package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FortuneWheelMechanic @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun getAvailableRewards(profileId: Int): List<FortuneReward> {
        val hasUnique = achievementRepository
            .getAchievementsByType(profileId, UNIQUE_FORTUNE_ACHIEVEMENT_TYPE)
            .any { it.unlocked }

        val profile = profileRepository.getProfileById(profileId)
        val canReceiveFreeze = (profile?.streakFreezes ?: 0) < MAX_FREEZES

        val baseRewards = mutableListOf(
            FortuneReward.Multiplier(2),
            FortuneReward.Multiplier(3),
            FortuneReward.Multiplier(5),
            FortuneReward.Xp(30),
            FortuneReward.Xp(60),
            FortuneReward.Xp(80)
        )

        if (canReceiveFreeze) baseRewards.add(FortuneReward.Freeze)
        return if (hasUnique) baseRewards else baseRewards + FortuneReward.UniqueAchievement
    }

    suspend fun processSpin(profileId: Int): EngineResult {
        val now = System.currentTimeMillis()

        val spinsToday = behaviorRepository.getBehaviorsByTypeAfter(
            profileId     = profileId,
            type          = "fortune_spin",
            fromTimestamp  = startOfToday(now)
        )

        if (spinsToday.size > 1) return EngineResult()

        val hasUnique = achievementRepository
            .getAchievementsByType(profileId, UNIQUE_FORTUNE_ACHIEVEMENT_TYPE)
            .any { it.unlocked }

        val profile = profileRepository.getProfileById(profileId)
        val canReceiveFreeze = (profile?.streakFreezes ?: 0) < MAX_FREEZES

        val options = mutableListOf(
            RewardOption(FortuneReward.Multiplier(2), 20.0),
            RewardOption(FortuneReward.Multiplier(3), 12.0),
            RewardOption(FortuneReward.Multiplier(5), 6.0),
            RewardOption(FortuneReward.Xp(30), 22.0),
            RewardOption(FortuneReward.Xp(60), 12.0),
            RewardOption(FortuneReward.Xp(80), 6.0)
        )

        if (!hasUnique) {
            options.add(RewardOption(FortuneReward.UniqueAchievement, 0.2))
        }
        if (canReceiveFreeze) {
            options.add(RewardOption(FortuneReward.Freeze, 8.0))
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
            FortuneReward.Freeze -> {
                profileRepository.addFreeze(profileId)
            }
        }

        return EngineResult(
            xpGranted     = if (reward is FortuneReward.Xp) reward.amount else 0,
            fortuneReward = reward
        )
    }


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
        const val MAX_FREEZES = 5
    }
}
