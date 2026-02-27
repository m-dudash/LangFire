package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.WordLevelProgress
import com.example.langfire_app.data.local.entities.WordProgressEntity

@Dao
interface WordProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: WordProgressEntity): Long

    @Query("SELECT * FROM word_progress WHERE profile_id = :profileId")
    suspend fun getAllByProfileId(profileId: Int): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE profile_id = :profileId AND word_id = :wordId")
    suspend fun getByWord(profileId: Int, wordId: Int): WordProgressEntity?

    @Query("SELECT COUNT(*) FROM word_progress WHERE profile_id = :profileId AND knowledge_coeff >= :threshold")
    suspend fun countLearnedWords(profileId: Int, threshold: Float): Int

    @Query("SELECT SUM(correct_count) FROM word_progress WHERE profile_id = :profileId")
    suspend fun getTotalCorrectCount(profileId: Int): Int?

    @Query("SELECT SUM(incorrect_count) FROM word_progress WHERE profile_id = :profileId")
    suspend fun getTotalIncorrectCount(profileId: Int): Int?

    @Update
    suspend fun update(progress: WordProgressEntity)

    @Query("DELETE FROM word_progress WHERE id = :id")
    suspend fun deleteById(id: Int)


    @Query("""
        SELECT w.word
        FROM word_progress wp
        JOIN words w ON wp.word_id = w.id
        WHERE wp.profile_id = :profileId
          AND wp.incorrect_count > 0
        ORDER BY wp.incorrect_count DESC,
                 wp.knowledge_coeff ASC
        LIMIT 1
    """)
    suspend fun getToughestWordText(profileId: Int): String?

    /**
     * Returns per-CEFR-level word mastery for a given profile.
     *
     */
    @Query("""
        SELECT
            l.name                                                        AS levelName,
            COUNT(DISTINCT w.id)                                          AS totalWords,
            COUNT(DISTINCT CASE
                WHEN wp.profile_id = :profileId
                 AND wp.knowledge_coeff >= :threshold
                THEN w.id END)                                            AS learnedWords
        FROM level l
        LEFT JOIN words  w  ON w.level_id  = l.id
        LEFT JOIN word_progress wp ON wp.word_id = w.id
        GROUP BY l.id, l.name
        ORDER BY l.id ASC
    """)
    suspend fun getWordLevelProgress(
        profileId: Int,
        threshold: Float
    ): List<WordLevelProgress>
}
