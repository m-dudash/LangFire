package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════
 *                     GAMIFICATION ENGINE
 *
 *   Central module in the Domain Layer that processes user behaviors
 *   and evaluates them against gamification rules to grant rewards.
 *
 *   Called after every relevant interaction:
 *   • App open (streak check, interval rules)
 *   • Session complete (points, achievements)
 *   • Word learned (progress milestones)
 *
 *   Three rule evaluation pipelines:
 *   1. SIMPLE       → Immediate attribute check
 *   2. REPETITIVE   → Accumulated history aggregation
 *   3. INTERVAL_REPETITIVE → Time-based pattern matching
 * ═══════════════════════════════════════════════════════════════════
 *
 * Architecture note: This class lives in the Domain Layer and depends only
 * on repository interfaces (not implementations). It has no Android or
 * framework dependencies and can be unit-tested independently.
 *
 * XP values are defined in the [Rule.xpReward] field (stored in the DB),
 * NOT hardcoded in this class.
 */
@Singleton
class GamificationEngine @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val behaviorRepository: BehaviorRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository,
    private val fortuneWheelMechanic: FortuneWheelMechanic
) {

    /**
     * Main entry point: process a user behavior through the gamification system.
     *
     * This method:
     * 1. Persists the behavior to the database
     * 2. Loads all gamification rules
     * 3. Evaluates each rule against the behavior (and history if needed)
     * 4. Grants/updates achievements for satisfied rules
     * 5. Awards XP defined in each satisfied rule
     * 6. Applies an active XP multiplier (if any)
     * 7. Updates streak
     * 8. Returns a summary of all rewards granted
     *
     * @param behavior The behavior to process (e.g., session_complete with attributes)
     * @return EngineResult containing all rewards and updates
     */
    suspend fun processBehavior(behavior: Behavior): EngineResult {
        // Step 1: Persist the behavior
        val savedId = behaviorRepository.saveBehavior(behavior)
        val savedBehavior = behavior.copy(id = savedId.toInt())

        // Step 2: Delegate fortune spin to its own mechanic
        if (savedBehavior.type == "fortune_spin") {
            return fortuneWheelMechanic.processSpin(savedBehavior.profileId)
        }

        // Step 3: Load all rules
        val rules = ruleRepository.getAllRules()

        // Step 4 & 5: Evaluate rules, collect results, calculate XP
        val newAchievements = mutableListOf<Achievement>()
        val updatedAchievements = mutableListOf<Achievement>()
        var totalXpGranted = 0

        for (rule in rules) {
            val satisfied = evaluateRule(rule, savedBehavior)
            if (satisfied) {
                val result = processReward(rule, savedBehavior.profileId)
                when (result) {
                    is RewardResult.NewlyUnlocked -> {
                        newAchievements.add(result.achievement)
                        totalXpGranted += computeRuleXp(rule, savedBehavior)
                    }
                    is RewardResult.Updated -> {
                        updatedAchievements.add(result.achievement)
                        totalXpGranted += computeRuleXp(rule, savedBehavior)
                    }
                    is RewardResult.AlreadyUnlocked -> {
                        // No action needed
                    }
                }
            }
        }

        // Step 6: Apply XP multiplier and persist
        if (totalXpGranted > 0) {
            val finalXp = applyXpMultiplierIfActive(savedBehavior.profileId, totalXpGranted)
            if (finalXp > 0) {
                profileRepository.addXp(savedBehavior.profileId, finalXp)
            }
        }

        // Step 7: Check & update streak
        val streakResult = updateStreakIfNeeded(savedBehavior)

        return EngineResult(
            xpGranted = totalXpGranted,
            newAchievements = newAchievements,
            updatedAchievements = updatedAchievements,
            streakUpdated = streakResult.first,
            newStreakDays = streakResult.second
        )
    }

    /**
     * Evaluate a single rule against the given behavior.
     * Routes to the appropriate evaluator based on rule type.
     */
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

    /**
     * Process the reward for a satisfied rule.
     *
     * Checks whether the associated achievement already exists:
     * - If it doesn't exist → create it (unlocked)
     * - If it exists but is locked → unlock it
     * - If it exists and is unlocked → update its value (for progressive achievements)
     */
    private suspend fun processReward(rule: Rule, profileId: Int): RewardResult {
        val existingAchievement = achievementRepository.getAchievementById(rule.achievementId)

        return if (existingAchievement == null) {
            val newAchievement = Achievement(
                id = rule.achievementId,
                type = "dynamic",
                value = 1,
                unlocked = true,
                description = "Achievement unlocked!",
                profileId = profileId
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

    /**
     * Update the user's daily streak if applicable.
     *
     * Logic:
     * - Calculates current streak from behavior history.
     * - If a day was MISSED and the user has streak freezes, one freeze is consumed
     *   and the streak is maintained (not reset).
     * - Awards a freeze every 10 streak days (if the user has < MAX_FREEZES).
     *
     * @return Pair(streakUpdated, newStreakDays)
     */
    private suspend fun updateStreakIfNeeded(behavior: Behavior): Pair<Boolean, Int> {
        val streakBehaviorTypes = setOf("session_complete", "app_open", "daily_activity")
        if (behavior.type !in streakBehaviorTypes) return Pair(false, 0)

        val profile = profileRepository.getProfileById(behavior.profileId) ?: return Pair(false, 0)

        val allBehaviors = behaviorRepository.getBehaviorsByProfile(behavior.profileId)
        val streakBehaviors = allBehaviors.filter { it.type in streakBehaviorTypes }

        val currentStreak = IntervalRepetitiveRuleEvaluator.getCurrentStreak(
            behaviors        = streakBehaviors,
            behaviorType     = behavior.type,
            interval         = "daily",
            currentTimestamp  = behavior.timestamp
        )

        // If currentConsecutive is 1 but we had a streak, it means we missed a day.
        // If the player has a freeze, consume it and RECOVER the streak (old streak + 1).
        val effectiveStreak = if (currentStreak == 1 && profile.streakDays > 0 && profile.streakFreezes > 0) {
            profileRepository.consumeFreeze(behavior.profileId)
            profile.streakDays + 1 // Add today to the preserved streak
        } else {
            currentStreak
        }

        // Award a freeze every 10 streak days (if below cap)
        if (effectiveStreak > 0
            && effectiveStreak % 10 == 0
            && effectiveStreak != profile.streakDays  // only on the day it hits the milestone
            && profile.streakFreezes < FortuneWheelMechanic.MAX_FREEZES
        ) {
            profileRepository.addFreeze(behavior.profileId)
        }

        if (effectiveStreak != profile.streakDays) {
            profileRepository.updateStreak(
                profileId      = behavior.profileId,
                streakDays     = effectiveStreak,
                lastActiveDate = behavior.timestamp
            )
            return Pair(true, effectiveStreak)
        }

        return Pair(false, profile.streakDays)
    }

    /**
     * Evaluate all rules without processing rewards.
     * Useful for checking which achievements are close to being unlocked
     * (for progress indicators in the UI).
     *
     * @return Map of Rule to Boolean (satisfied or not)
     */
    suspend fun evaluateAllRules(profileId: Int, behavior: Behavior): Map<Rule, Boolean> {
        val rules = ruleRepository.getAllRules()
        return rules.associateWith { rule -> evaluateRule(rule, behavior) }
    }

    /**
     * Get the current streak for a profile.
     * Convenience method for UI display.
     */
    suspend fun getCurrentStreak(profileId: Int): Int {
        val allBehaviors = behaviorRepository.getBehaviorsByProfile(profileId)
        val streakTypes = setOf("session_complete", "app_open", "daily_activity")
        val streakBehaviors = allBehaviors.filter { it.type in streakTypes }

        return if (streakBehaviors.isNotEmpty()) {
            IntervalRepetitiveRuleEvaluator.getCurrentStreak(
                behaviors     = streakBehaviors,
                behaviorType  = streakBehaviors.first().type,
                interval      = "daily"
            )
        } else {
            0
        }
    }

    /**
     * Get available rewards for the fortune wheel.
     */
    suspend fun getFortuneRewards(profileId: Int): List<FortuneReward> {
        return fortuneWheelMechanic.getAvailableRewards(profileId)
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /**
     * Compute XP for a single satisfied rule.
     *
     * If [RuleConditions.xpAttribute] is set, multiplies [Rule.xpReward]
     * by the integer value of that attribute in the behavior.
     * Otherwise returns [Rule.xpReward] as-is.
     */
    private fun computeRuleXp(rule: Rule, behavior: Behavior): Int {
        val xpAttr = rule.conditions.xpAttribute
        if (xpAttr != null) {
            val multiplier = behavior.attributes[xpAttr]?.toIntOrNull() ?: 1
            return rule.xpReward * multiplier
        }
        return rule.xpReward
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

    /**
     * Sealed class representing the result of processing a reward.
     */
    private sealed class RewardResult {
        data class NewlyUnlocked(val achievement: Achievement) : RewardResult()
        data class Updated(val achievement: Achievement) : RewardResult()
        data object AlreadyUnlocked : RewardResult()
    }
}
