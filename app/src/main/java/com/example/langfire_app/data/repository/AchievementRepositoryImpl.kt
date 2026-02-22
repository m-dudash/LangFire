package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.AchievementDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.Achievement
import com.example.langfire_app.domain.repository.AchievementRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override suspend fun saveAchievement(achievement: Achievement): Long {
        return achievementDao.insert(achievement.toEntity())
    }

    override suspend fun saveAllAchievements(achievements: List<Achievement>) {
        achievementDao.insertAll(achievements.map { it.toEntity() })
    }

    override suspend fun getAchievementById(id: Int): Achievement? {
        return achievementDao.getById(id)?.toDomain()
    }

    override suspend fun getAchievementsByProfile(profileId: Int): List<Achievement> {
        return achievementDao.getAllByProfileId(profileId).map { it.toDomain() }
    }

    override suspend fun getAchievementsByType(profileId: Int, type: String): List<Achievement> {
        return achievementDao.getByType(profileId, type).map { it.toDomain() }
    }

    override suspend fun getUnlockedAchievements(profileId: Int): List<Achievement> {
        return achievementDao.getUnlocked(profileId).map { it.toDomain() }
    }

    override suspend fun getLockedAchievements(profileId: Int): List<Achievement> {
        return achievementDao.getLocked(profileId).map { it.toDomain() }
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        achievementDao.update(achievement.toEntity())
    }
}
