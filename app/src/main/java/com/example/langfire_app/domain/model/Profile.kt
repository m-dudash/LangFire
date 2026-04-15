package com.example.langfire_app.domain.model

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
        const val GOAL_BEGINNER   = 15
        const val GOAL_MODERATE   = 25
        const val GOAL_INTENSIVE  = 35
        const val GOAL_BURN       = 50

        const val DEFAULT_DAILY_GOAL = GOAL_BEGINNER

        val GOAL_TIERS = listOf(GOAL_BEGINNER, GOAL_MODERATE, GOAL_INTENSIVE, GOAL_BURN)
    }
}
