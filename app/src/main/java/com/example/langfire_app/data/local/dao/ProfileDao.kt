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

    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun deleteById(id: Int)
}
