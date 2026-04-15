package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.RuleEntity

@Dao
interface RuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RuleEntity>)

    @Query("SELECT * FROM rule")
    suspend fun getAll(): List<RuleEntity>

    @Query("SELECT * FROM rule WHERE id = :id")
    suspend fun getById(id: Int): RuleEntity?

    @Query("SELECT * FROM rule WHERE type = :type")
    suspend fun getByType(type: String): List<RuleEntity>

    @Query("SELECT * FROM rule WHERE achievement_id = :achievementId")
    suspend fun getByAchievementId(achievementId: Int): List<RuleEntity>

    @Update
    suspend fun update(rule: RuleEntity)

    @Query("DELETE FROM rule WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM rule")
    suspend fun deleteAll()
}
