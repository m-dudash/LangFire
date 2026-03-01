package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.ProfileEntity

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity): Long

    @Query("SELECT * FROM profile WHERE id = :id")
    suspend fun getById(id: Int): ProfileEntity?

    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getActiveProfile(): ProfileEntity?

    @Update
    suspend fun update(profile: ProfileEntity)

    @Query("UPDATE profile SET xp = xp + :amount WHERE id = :profileId")
    suspend fun addXp(profileId: Int, amount: Int)

    @Query("UPDATE profile SET streak_days = :streakDays, last_active_date = :lastActiveDate WHERE id = :profileId")
    suspend fun updateStreak(profileId: Int, streakDays: Int, lastActiveDate: Long)

    @Query("""
        UPDATE profile 
        SET xp_multiplier = :multiplier, xp_multiplier_expires_at = :expiresAt 
        WHERE id = :profileId
    """)
    suspend fun setXpMultiplier(profileId: Int, multiplier: Int, expiresAt: Long?)

    @Query("""
        UPDATE profile 
        SET xp_multiplier = 1, xp_multiplier_expires_at = NULL 
        WHERE id = :profileId
    """)
    suspend fun clearXpMultiplier(profileId: Int)

    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun deleteById(id: Int)
}
