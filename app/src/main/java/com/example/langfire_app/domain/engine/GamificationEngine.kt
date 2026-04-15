package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Singleton
class GamificationEngine @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val behaviorRepository: BehaviorRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun processBehavior(behavior: Behavior): EngineResult {
        val savedBehavior = persistBehavior(behavior)
        if (savedBehavior.type == "fortune_spin") {
            return processFortuneSpin(savedBehavior.profileId)
        }
        val rules = getRulesForBehaviorType(savedBehavior.type)
        val rewardsSummary = processRulesAndGatherRewards(rules, savedBehavior)
        val finalXp = grantXpToProfile(savedBehavior.profileId, rewardsSummary.totalXp)
        val (streakUpdated, newStreakDays) = if (savedBehavior.type == "session_complete") {
            checkAndTriggerDailyGoal(savedBehavior)
        } else {
            updateStreakIfNeeded(savedBehavior)
        }
        return EngineResult(
            xpGranted = finalXp,
            newAchievements = rewardsSummary.newAchievements,
            updatedAchievements = rewardsSummary.updatedAchievements,
            streakUpdated = streakUpdated,
            newStreakDays = newStreakDays,
            freezeGranted = rewardsSummary.freezeGranted
        )
    }

    private data class RewardsSummary(
        val newAchievements: List<Achievement> = emptyList(),
        val updatedAchievements: List<Achievement> = emptyList(),
        val totalXp: Int = 0,
        val freezeGranted: Boolean = false
    )

    private suspend fun persistBehavior(behavior: Behavior): Behavior {
        val savedId = behaviorRepository.saveBehavior(behavior)
        return behavior.copy(id = savedId.toInt())
    }

    private suspend fun getRulesForBehaviorType(behaviorType: String): List<Rule> {
        return ruleRepository.getAllRules().filter {
            it.conditions.behaviorType == behaviorType
        }
    }

    private suspend fun processRulesAndGatherRewards(rules: List<Rule>, behavior: Behavior): RewardsSummary {
        val newAchievements = mutableListOf<Achievement>()
        val updatedAchievements = mutableListOf<Achievement>()
        var totalXpGranted = 0
        var freezeGranted = false

        for (rule in rules) {
            val isSatisfied = evaluateRule(rule, behavior)
            if (!isSatisfied) continue
            val ruleXp = calculateRuleXp(rule, behavior)
            when (val result = processReward(rule, behavior.profileId)) {
                is RewardResult.NewlyUnlocked -> {
                    newAchievements.add(result.achievement)
                    if (!rule.conditions.repeatableXp) {
                        totalXpGranted += if (ruleXp > 0) ruleXp
                        else calculateXpForAchievement(result.achievement)
                    }
                }
                is RewardResult.Updated -> {
                    updatedAchievements.add(result.achievement)
                }
                is RewardResult.AlreadyUnlocked -> Unit
            }
            if (rule.conditions.repeatableXp && ruleXp > 0) {
                totalXpGranted += ruleXp
            }
            if (rule.grantsFreeze) {
                profileRepository.addFreeze(behavior.profileId)
                freezeGranted = true
            }
        }
        return RewardsSummary(newAchievements, updatedAchievements, totalXpGranted, freezeGranted)
    }

    private fun calculateRuleXp(rule: Rule, behavior: Behavior): Int {
        var ruleXp = rule.xpReward
        if (rule.conditions.xpAttribute != null) {
            val multiplier = behavior.attributes[rule.conditions.xpAttribute]?.toIntOrNull() ?: 1
            ruleXp *= multiplier
        }
        return ruleXp
    }


    private suspend fun grantXpToProfile(profileId: Int, baseXp: Int): Int {
        if (baseXp <= 0) return 0
        val finalXp = applyXpMultiplierIfActive(profileId, baseXp)
        if (finalXp > 0) {
            profileRepository.addXp(profileId, finalXp)
        }
        return finalXp
    }

    private suspend fun evaluateRule(rule: Rule, behavior: Behavior): Boolean {
        return when (rule.type) {
            RuleType.SIMPLE -> {
                SimpleRuleEvaluator.evaluate(rule, behavior)
            }
            RuleType.REPETITIVE -> {
                val history = behaviorRepository.getBehaviorsByType(
                    behavior.profileId,
                    rule.conditions.behaviorType
                )
                RepetitiveRuleEvaluator.evaluate(rule, behavior, history)
            }
            RuleType.INTERVAL_REPETITIVE -> {
                val history = behaviorRepository.getBehaviorsByType(
                    behavior.profileId,
                    rule.conditions.behaviorType
                )
                IntervalRepetitiveRuleEvaluator.evaluate(rule, behavior, history)
            }
        }
    }


    private suspend fun processReward(rule: Rule, profileId: Int): RewardResult {
        val existingAchievement = achievementRepository.getAchievementById(rule.achievementId)
        if (existingAchievement != null && existingAchievement.type == "xp_only") {
            return RewardResult.AlreadyUnlocked
        }
        return if (existingAchievement == null) {
            val newAchievement = Achievement(
                id = rule.achievementId, type = "dynamic", value = 1, unlocked = true,
                description = "Achievement unlocked!", profileId = profileId
            )
            achievementRepository.saveAchievement(newAchievement)
            RewardResult.NewlyUnlocked(newAchievement)
        } else if (!existingAchievement.unlocked) {
            val unlockedAchievement = existingAchievement.copy(unlocked = true)
            achievementRepository.updateAchievement(unlockedAchievement)
            RewardResult.NewlyUnlocked(unlockedAchievement)
        } else {
            val updatedAchievement = existingAchievement.copy(
                value = (existingAchievement.value ?: 0) + 1
            )
            achievementRepository.updateAchievement(updatedAchievement)
            RewardResult.Updated(updatedAchievement)
        }
    }

    private fun calculateXpForAchievement(achievement: Achievement): Int {
        return when (achievement.type) {
            "streak"     -> XP_STREAK_ACHIEVEMENT
            "accuracy"   -> XP_ACCURACY_ACHIEVEMENT
            "word_count" -> XP_WORD_COUNT_ACHIEVEMENT
            "speed"      -> XP_SPEED_ACHIEVEMENT
            "rare"       -> XP_RARE_ACHIEVEMENT
            else         -> XP_DEFAULT_ACHIEVEMENT
        }
    }

    private suspend fun updateStreakIfNeeded(behavior: Behavior): Pair<Boolean, Int> {
        val streakBehaviorTypes = setOf("daily_activity", "app_open")
        if (behavior.type !in streakBehaviorTypes) return Pair(false, 0)

        var profile = profileRepository.getProfileById(behavior.profileId) ?: return Pair(false, 0)

        var allBehaviors = behaviorRepository.getBehaviorsByProfile(profile.id)
        var streakBehaviors = allBehaviors.filter { it.type == "daily_activity" || it.type == "freeze_used" }

        if (streakBehaviors.isNotEmpty()) {
            val targetDay = getLocalDayEpoch(behavior.timestamp)
            val lastValidDayTimestamp = streakBehaviors.maxOf { it.timestamp }
            val lastValidDay = getLocalDayEpoch(lastValidDayTimestamp)

            val gap = targetDay - lastValidDay
            if (gap > 1) {
                val missedDays = (gap - 1).toInt()

                if (profile.streakFreezes >= missedDays && profile.streakDays > 0) {
                    for (i in 1..missedDays) {
                        profileRepository.consumeFreeze(profile.id)

                        val missedDayTimestamp = behavior.timestamp - (i * 24 * 60 * 60 * 1000L)
                        behaviorRepository.saveBehavior(
                            Behavior(
                                type = "freeze_used",
                                timestamp = missedDayTimestamp,
                                profileId = profile.id
                            )
                        )
                    }

                    profile = profileRepository.getProfileById(profile.id)!!
                    allBehaviors = behaviorRepository.getBehaviorsByProfile(profile.id)
                    streakBehaviors = allBehaviors.filter { it.type == "daily_activity" || it.type == "freeze_used" }
                }
            }
        }

        val currentStreak = IntervalRepetitiveRuleEvaluator.getCurrentStreak(
            behaviors = streakBehaviors,
            interval = "daily",
            currentTimestamp = behavior.timestamp
        )

        if (currentStreak != profile.streakDays) {
            profileRepository.updateStreak(
                profileId = profile.id,
                streakDays = currentStreak,
                lastActiveDate = behavior.timestamp
            )
            return Pair(true, currentStreak)
        }

        return Pair(false, profile.streakDays)
    }

    private fun getLocalDayEpoch(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).apply {
            timeInMillis = timestamp
        }
        val offset = cal.timeZone.getOffset(timestamp)
        return (timestamp + offset) / (24 * 60 * 60 * 1000L)
    }


    suspend fun evaluateAllRules(profileId: Int, behavior: Behavior): Map<Rule, Boolean> {
        val rules = ruleRepository.getAllRules()
        return rules.associateWith { rule -> evaluateRule(rule, behavior) }
    }


    suspend fun getCurrentStreak(profileId: Int): Int {
        val allBehaviors = behaviorRepository.getBehaviorsByProfile(profileId)
        val streakBehaviors = allBehaviors.filter { it.type == "daily_activity" }

        return IntervalRepetitiveRuleEvaluator.getCurrentStreak(
            behaviors = streakBehaviors,
            interval = "daily"
        )
    }


    suspend fun getFortuneRewards(profileId: Int): List<FortuneReward> {
        val profile = profileRepository.getProfileById(profileId)
        val hasUnique = achievementRepository
            .getAchievementsByType(profileId, UNIQUE_FORTUNE_ACHIEVEMENT_TYPE)
            .isNotEmpty()

        val list = mutableListOf<FortuneReward>(
            FortuneReward.Multiplier(2),
            FortuneReward.Multiplier(3),
            FortuneReward.Multiplier(5),
            FortuneReward.Xp(30),
            FortuneReward.Xp(60),
            FortuneReward.Xp(80)
        )

        if (profile == null || profile.streakFreezes < 5) {
            list.add(FortuneReward.Freeze)
        }

        if (!hasUnique) {
            list.add(FortuneReward.UniqueAchievement)
        }
        return list
    }

    private suspend fun processFortuneSpin(profileId: Int): EngineResult {
        val now = System.currentTimeMillis()

        val spinsToday = behaviorRepository.getBehaviorsByTypeAfter(
            profileId = profileId,
            type = "fortune_spin",
            fromTimestamp = startOfToday(now)
        )

        if (spinsToday.size > 1) {
            return EngineResult()
        }

        val rewards = getFortuneRewards(profileId)
        val weightsMap = mapOf(
            FortuneReward.Multiplier(2)      to 20.0,
            FortuneReward.Multiplier(3)      to 12.0,
            FortuneReward.Multiplier(5)      to 6.0,
            FortuneReward.Xp(30)              to 22.0,
            FortuneReward.Xp(60)              to 12.0,
            FortuneReward.Xp(80)              to 6.0,
            FortuneReward.Freeze            to 7.0,
            FortuneReward.UniqueAchievement to 0.2
        )

        val options = rewards.map { RewardOption(it, weightsMap[it] ?: 1.0) }

        val reward = pickWeightedReward(options)

        when (reward) {
            is FortuneReward.Xp -> profileRepository.addXp(profileId, reward.amount)
            is FortuneReward.Multiplier -> {
                val expiresAt = now + TimeUnit.HOURS.toMillis(FORTUNE_MULTIPLIER_HOURS)
                profileRepository.setXpMultiplier(profileId, reward.multiplier, expiresAt)
            }
            FortuneReward.UniqueAchievement -> {
                val achievement = Achievement(
                    type = UNIQUE_FORTUNE_ACHIEVEMENT_TYPE,
                    unlocked = true,
                    description = "Unique fortune wheel reward",
                    profileId = profileId
                )
                achievementRepository.saveAchievement(achievement)
            }
            FortuneReward.Freeze -> {
                profileRepository.addFreeze(profileId)
            }
        }

        return EngineResult(
            xpGranted = if (reward is FortuneReward.Xp) reward.amount else 0,
            fortuneReward = reward,
            freezeGranted = reward is FortuneReward.Freeze
        )
    }

    private fun startOfToday(now: Long): Long {
        val days = TimeUnit.MILLISECONDS.toDays(now)
        return TimeUnit.DAYS.toMillis(days)
    }


    private suspend fun checkAndTriggerDailyGoal(sessionBehavior: Behavior): Pair<Boolean, Int> {
        val profile = profileRepository.getProfileById(sessionBehavior.profileId)
            ?: return Pair(false, 0)

        val todayStart = startOfToday(sessionBehavior.timestamp)

        val existingDailyActivity = behaviorRepository.getBehaviorsByTypeAfter(
            profileId = sessionBehavior.profileId,
            type = "daily_activity",
            fromTimestamp = todayStart
        )
        if (existingDailyActivity.isNotEmpty()) return Pair(false, profile.streakDays)

        val todaySessions = behaviorRepository.getBehaviorsByTypeAfter(
            profileId = sessionBehavior.profileId,
            type = "session_complete",
            fromTimestamp = todayStart
        )
        val correctToday = todaySessions.sumOf {
            it.attributes["correct_count"]?.toIntOrNull() ?: 0
        }

        if (correctToday >= profile.dailyWordGoal) {
            val dailyBehavior = Behavior(
                type = "daily_activity",
                profileId = sessionBehavior.profileId,
                timestamp = sessionBehavior.timestamp
            )
            val savedId = behaviorRepository.saveBehavior(dailyBehavior)
            return updateStreakIfNeeded(dailyBehavior.copy(id = savedId.toInt()))
        }

        return Pair(false, profile.streakDays)
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

    private suspend fun applyXpMultiplierIfActive(profileId: Int, baseXp: Int): Int {
        val profile = profileRepository.getProfileById(profileId) ?: return baseXp
        val multiplier = profile.xpMultiplier
        val expiresAt = profile.xpMultiplierExpiresAt
        val now = System.currentTimeMillis()

        if (multiplier > 1 && expiresAt != null) {
            if (now < expiresAt) {
                return baseXp * multiplier
            }
            profileRepository.clearXpMultiplier(profileId)
        }

        return baseXp
    }

    private fun isSameDay(a: Long, b: Long): Boolean {
        val dayA = TimeUnit.MILLISECONDS.toDays(a)
        val dayB = TimeUnit.MILLISECONDS.toDays(b)
        return dayA == dayB
    }

    private data class RewardOption(
        val reward: FortuneReward,
        val weight: Double
    )

    private sealed class RewardResult {
        data class NewlyUnlocked(val achievement: Achievement) : RewardResult()
        data class Updated(val achievement: Achievement) : RewardResult()
        data object AlreadyUnlocked : RewardResult()
    }

    companion object {
        const val XP_STREAK_ACHIEVEMENT = 50
        const val XP_ACCURACY_ACHIEVEMENT = 30
        const val XP_WORD_COUNT_ACHIEVEMENT = 40
        const val XP_SPEED_ACHIEVEMENT = 25
        const val XP_RARE_ACHIEVEMENT = 100
        const val XP_DEFAULT_ACHIEVEMENT = 20

        const val UNIQUE_FORTUNE_ACHIEVEMENT_TYPE = "unique_fortune_reward"
        const val FORTUNE_MULTIPLIER_HOURS = 4L
    }
}
