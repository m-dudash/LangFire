package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Achievement

interface AchievementRepository {
    suspend fun saveAchievement(achievement: Achievement): Long
    suspend fun saveAllAchievements(achievements: List<Achievement>)
    suspend fun getAchievementById(id: Int): Achievement?
    suspend fun getAchievementsByProfile(profileId: Int): List<Achievement>
    suspend fun getAchievementsByType(profileId: Int, type: String): List<Achievement>
    suspend fun getUnlockedAchievements(profileId: Int): List<Achievement>
    suspend fun getLockedAchievements(profileId: Int): List<Achievement>
    suspend fun updateAchievement(achievement: Achievement)
}
