package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

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
 */
@Singleton
class GamificationEngine @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val behaviorRepository: BehaviorRepository,
    private val achievementRepository: AchievementRepository,
    private val profileRepository: ProfileRepository
) {

    /**
     * Main entry point: process a user behavior through the gamification system.
     *
     * This method:
     * 1. Persists the behavior to the database
     * 2. Loads all gamification rules
     * 3. Evaluates each rule against the behavior (and history if needed)
     * 4. Grants/updates achievements for satisfied rules
     * 5. Awards XP for newly unlocked achievements
     * 6. Returns a summary of all rewards granted
     *
     * @param behavior The behavior to process (e.g., session_complete with attributes)
     * @return EngineResult containing all rewards and updates
     */
    suspend fun processBehavior(behavior: Behavior): EngineResult {
        // Step 1: Persist the behavior
        val savedId = behaviorRepository.saveBehavior(behavior)
        val savedBehavior = behavior.copy(id = savedId.toInt())

        // Step 2: Load all rules
        val rules = ruleRepository.getAllRules()

        // Step 3 & 4: Evaluate rules and collect results
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
                        totalXpGranted += calculateXpForAchievement(result.achievement)
                    }
                    is RewardResult.Updated -> {
                        updatedAchievements.add(result.achievement)
                    }
                    is RewardResult.AlreadyUnlocked -> {
                        // No action needed
                    }
                }
            }
        }

        // Step 5: Award XP
        if (totalXpGranted > 0) {
            profileRepository.addXp(savedBehavior.profileId, totalXpGranted)
        }

        // Step 6: Check & update streak (always check on relevant behaviors)
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
            // Achievement doesn't exist yet — create as unlocked
            // This handles the case where achievements are created dynamically
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
            // Achievement exists but is locked — unlock it
            val unlockedAchievement = existingAchievement.copy(unlocked = true)
            achievementRepository.updateAchievement(unlockedAchievement)
            RewardResult.NewlyUnlocked(unlockedAchievement)
        } else {
            // Achievement already unlocked — update value (progressive)
            val updatedAchievement = existingAchievement.copy(
                value = (existingAchievement.value ?: 0) + 1
            )
            achievementRepository.updateAchievement(updatedAchievement)
            RewardResult.Updated(updatedAchievement)
        }
    }

    /**
     * Calculate XP reward for a newly unlocked achievement.
     * XP values can be tied to achievement types.
     */
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

    /**
     * Update the user's daily streak if applicable.
     *
     * Uses IntervalRepetitiveRuleEvaluator to calculate the current streak
     * based on "daily_activity" behaviors, then updates profile.
     *
     * @return Pair(streakUpdated, newStreakDays)
     */
    private suspend fun updateStreakIfNeeded(behavior: Behavior): Pair<Boolean, Int> {
        // Only update streak on certain behavior types
        val streakBehaviorTypes = setOf("session_complete", "app_open", "daily_activity")
        if (behavior.type !in streakBehaviorTypes) return Pair(false, 0)

        val profile = profileRepository.getProfileById(behavior.profileId) ?: return Pair(false, 0)

        // Get all daily activity behaviors for streak calculation
        val allBehaviors = behaviorRepository.getBehaviorsByProfile(behavior.profileId)
        val streakBehaviors = allBehaviors.filter { it.type in streakBehaviorTypes }

        val currentStreak = IntervalRepetitiveRuleEvaluator.getCurrentStreak(
            behaviors = streakBehaviors,
            behaviorType = behavior.type,
            interval = "daily",
            currentTimestamp = behavior.timestamp
        )

        if (currentStreak != profile.streakDays) {
            profileRepository.updateStreak(
                profileId = behavior.profileId,
                streakDays = currentStreak,
                lastActiveDate = behavior.timestamp
            )
            return Pair(true, currentStreak)
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
                behaviors = streakBehaviors,
                behaviorType = streakBehaviors.first().type,
                interval = "daily"
            )
        } else {
            0
        }
    }

    /**
     * Sealed class representing the result of processing a reward.
     */
    private sealed class RewardResult {
        data class NewlyUnlocked(val achievement: Achievement) : RewardResult()
        data class Updated(val achievement: Achievement) : RewardResult()
        data object AlreadyUnlocked : RewardResult()
    }

    companion object {
        // XP values for different achievement types
        const val XP_STREAK_ACHIEVEMENT = 50
        const val XP_ACCURACY_ACHIEVEMENT = 30
        const val XP_WORD_COUNT_ACHIEVEMENT = 40
        const val XP_SPEED_ACHIEVEMENT = 25
        const val XP_RARE_ACHIEVEMENT = 100
        const val XP_DEFAULT_ACHIEVEMENT = 20
    }
}
