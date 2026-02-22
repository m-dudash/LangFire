package com.example.langfire_app.domain.model

/**
 * Result returned by the Gamification Engine after processing a behavior.
 *
 * Contains all the rewards and updates that occurred as a consequence
 * of the behavior being processed against the rule set.
 *
 * @param xpGranted Total XP granted during this processing cycle
 * @param newAchievements List of newly unlocked achievements
 * @param updatedAchievements List of achievements that had their value updated
 * @param streakUpdated Whether the daily streak was updated
 * @param newStreakDays The new streak count (if updated)
 */
data class EngineResult(
    val xpGranted: Int = 0,
    val newAchievements: List<Achievement> = emptyList(),
    val updatedAchievements: List<Achievement> = emptyList(),
    val streakUpdated: Boolean = false,
    val newStreakDays: Int = 0
)
