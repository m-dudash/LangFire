package com.example.langfire_app.domain.model

/**
 * Domain model representing a user profile.
 * Independent of any framework or database implementation.
 */
data class Profile(
    val id: Int = 0,
    val name: String,
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val xpMultiplier: Int = 1,
    val xpMultiplierExpiresAt: Long? = null,
    val avatarPath: String? = null,
    val streakFreezes: Int = 0,
    val dailyWordGoal: Int = DEFAULT_DAILY_GOAL
) {
    companion object {
        /** Available daily-goal tiers (correct answers per day). */
        const val GOAL_BEGINNER   = 15   // 🌱 Beginner
        const val GOAL_MODERATE   = 25   // 🤔 You decide
        const val GOAL_INTENSIVE  = 35   // 🔥 Intensive
        const val GOAL_BURN       = 50   // 💀 BURN IT!

        const val DEFAULT_DAILY_GOAL = GOAL_BEGINNER

        val GOAL_TIERS = listOf(GOAL_BEGINNER, GOAL_MODERATE, GOAL_INTENSIVE, GOAL_BURN)
    }
}
