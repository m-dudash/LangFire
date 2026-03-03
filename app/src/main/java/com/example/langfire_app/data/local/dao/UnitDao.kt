package com.example.langfire_app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.langfire_app.data.local.entities.UnitWithStats

@Dao
interface UnitDao {

    @Query("""
        SELECT 
            u.id AS unitId,
            u.name AS unitName,
            l.name AS levelName,
            l.id AS levelId,
            COUNT(w.id) AS totalWords,
            COUNT(CASE WHEN wp.knowledge_coeff >= :learnedThreshold THEN 1 END) AS learnedWords
        FROM unit u
        JOIN level l ON u.level_id = l.id
        LEFT JOIN words w ON w.unit_id = u.id
        LEFT JOIN word_progress wp ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
        GROUP BY u.id, l.id
        ORDER BY l.id ASC, u.id ASC
    """)
    suspend fun getUnitsWithStats(
        courseId: Int,
        profileId: Int,
        learnedThreshold: Float
    ): List<UnitWithStats>
}