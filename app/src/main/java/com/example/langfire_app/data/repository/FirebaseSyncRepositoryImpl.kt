package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.*
import com.example.langfire_app.data.local.entities.AppSettingEntity
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.UserStatsBackup
import com.example.langfire_app.domain.repository.AuthRepository
import com.example.langfire_app.domain.repository.SyncRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val db: FirebaseDatabase,
    private val profileDao: ProfileDao,
    private val wordProgressDao: WordProgressDao,
    private val achievementDao: AchievementDao,
    private val behaviorDao: BehaviorDao,
    private val appSettingsDao: AppSettingsDao,
    private val gson: Gson
) : SyncRepository {

    override suspend fun uploadStats(stats: UserStatsBackup): Result<Unit> {
        val userId = authRepository.currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val statsJson = gson.toJson(stats)
            db.getReference("users").child(userId).child("stats").setValue(statsJson).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadStats(): Result<UserStatsBackup?> {
        val userId = authRepository.currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = db.getReference("users").child(userId).child("stats").get().await()
            val statsJson = snapshot.getValue(String::class.java)
            if (statsJson != null) {
                val stats = gson.fromJson(statsJson, UserStatsBackup::class.java)
                Result.success(stats)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocalStats(): UserStatsBackup {
        val profileEntity = profileDao.getActiveProfile() ?: throw Exception("No active profile")
        val profile = profileEntity.toDomain()
        val wordProgress = wordProgressDao.getAllByProfileId(profileEntity.id).map { it.toDomain() }
        val achievements = achievementDao.getAllByProfileId(profileEntity.id).map { it.toDomain() }
        val behaviors = behaviorDao.getAllByProfileId(profileEntity.id).map { it.toDomain() }
        val lastCourseId = appSettingsDao.getSetting("current_course_id")?.toIntOrNull()
        return UserStatsBackup(profile, wordProgress, achievements, behaviors, lastCourseId)
    }

    override suspend fun restoreStats(stats: UserStatsBackup) {
        val existingProfile = profileDao.getActiveProfile()
        if (existingProfile != null) {
            val pid = existingProfile.id
            wordProgressDao.deleteAllByProfileId(pid)
            achievementDao.deleteAllByProfileId(pid)
            behaviorDao.deleteAllByProfileId(pid)
            profileDao.deleteById(pid)
        }

        profileDao.insert(stats.profile.toEntity())
        
        stats.wordProgress.forEach {
            wordProgressDao.insert(it.toEntity())
        }
        
        achievementDao.insertAll(stats.achievements.map { it.toEntity() })

        stats.behaviors.forEach {
            behaviorDao.insert(it.toEntity())
        }

        stats.lastCourseId?.let { courseId ->
            appSettingsDao.insert(AppSettingEntity("current_course_id", courseId.toString()))
        }
    }
}
