package com.example.langfire_app.domain.model


data class EngineResult(
    val xpGranted: Int = 0,
    val newAchievements: List<Achievement> = emptyList(),
    val updatedAchievements: List<Achievement> = emptyList(),
    val streakUpdated: Boolean = false,
    val newStreakDays: Int = 0,
    val fortuneReward: FortuneReward? = null,
    val freezeGranted: Boolean = false,
    val correctToday: Int = 0,
    val dailyGoal: Int = 0
)
