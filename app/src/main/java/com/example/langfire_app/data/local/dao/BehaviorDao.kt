package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.BehaviorEntity

@Dao
interface BehaviorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(behavior: BehaviorEntity): Long

    @Query("SELECT * FROM behavior WHERE profile_id = :profileId ORDER BY timestamp DESC")
    suspend fun getAllByProfileId(profileId: Int): List<BehaviorEntity>

    @Query("SELECT * FROM behavior WHERE profile_id = :profileId AND type = :type ORDER BY timestamp DESC")
    suspend fun getByType(profileId: Int, type: String): List<BehaviorEntity>

    @Query("""
        SELECT * FROM behavior 
        WHERE profile_id = :profileId 
          AND type = :type 
          AND timestamp >= :fromTimestamp 
        ORDER BY timestamp DESC
    """)
    suspend fun getByTypeAfter(profileId: Int, type: String, fromTimestamp: Long): List<BehaviorEntity>

    @Query("""
        SELECT * FROM behavior 
        WHERE profile_id = :profileId 
          AND type = :type 
          AND timestamp BETWEEN :fromTimestamp AND :toTimestamp 
        ORDER BY timestamp DESC
    """)
    suspend fun getByTypeInRange(
        profileId: Int,
        type: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<BehaviorEntity>

    @Query("DELETE FROM behavior WHERE id = :id")
    suspend fun deleteById(id: Int)
}
