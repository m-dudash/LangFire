package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.AchievementEntity

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM achievement WHERE profile_id = :profileId")
    suspend fun getAllByProfileId(profileId: Int): List<AchievementEntity>

    @Query("SELECT * FROM achievement WHERE id = :id")
    suspend fun getById(id: Int): AchievementEntity?

    @Query("SELECT * FROM achievement WHERE profile_id = :profileId AND type = :type")
    suspend fun getByType(profileId: Int, type: String): List<AchievementEntity>

    @Query("SELECT * FROM achievement WHERE profile_id = :profileId AND unlocked = 1")
    suspend fun getUnlocked(profileId: Int): List<AchievementEntity>

    @Query("SELECT * FROM achievement WHERE profile_id = :profileId AND unlocked = 0")
    suspend fun getLocked(profileId: Int): List<AchievementEntity>

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("DELETE FROM achievement WHERE id = :id")
    suspend fun deleteById(id: Int)
}
