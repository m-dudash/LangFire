package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.WordLevelProgress
import com.example.langfire_app.data.local.entities.WordProgressEntity

/**
 * A word item used in the stats bottom sheet, carries unit name.
 */
data class StatWordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val unitName: String,
    val knowledgeCoeff: Float?
)

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

    /**
     * Returns per-CEFR-level word mastery for a given profile AND course.
     */
    @Query("""
        SELECT
            l.name                                                        AS levelName,
            COUNT(DISTINCT CASE
                WHEN u.course_id = :courseId
                THEN w.id END)                                            AS totalWords,
            COUNT(DISTINCT CASE
                WHEN u.course_id = :courseId
                 AND wp.profile_id = :profileId
                 AND wp.knowledge_coeff >= :threshold
                THEN w.id END)                                            AS learnedWords
        FROM level l
        LEFT JOIN words  w  ON w.level_id  = l.id
        LEFT JOIN unit   u  ON u.id = w.unit_id
        LEFT JOIN word_progress wp ON wp.word_id = w.id
        GROUP BY l.id, l.name
        ORDER BY l.id ASC
    """)
    suspend fun getWordLevelProgressByCourse(
        profileId: Int,
        courseId: Int,
        threshold: Float
    ): List<WordLevelProgress>


    @Query("""
    SELECT COUNT(DISTINCT wp.word_id)
    FROM word_progress wp
    JOIN words w ON w.id = wp.word_id
    JOIN unit u ON u.id = w.unit_id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
""")
    suspend fun countToLearnByCourse(
        profileId: Int,
        courseId: Int
    ): Int

    @Query("""
    SELECT COUNT(DISTINCT wp.word_id)
    FROM word_progress wp
    JOIN words w ON w.id = wp.word_id
    JOIN unit u ON u.id = w.unit_id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
      AND COALESCE(wp.knowledge_coeff, 0) > :threshold
""")
    suspend fun countPracticedByCourse(
        profileId: Int,
        courseId: Int,
        threshold: Float = 0.30f
    ): Int

    @Query("""
    SELECT COUNT(DISTINCT wp.word_id)
    FROM word_progress wp
    JOIN words w ON w.id = wp.word_id
    JOIN unit u ON u.id = w.unit_id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
      AND COALESCE(wp.knowledge_coeff, 0) >= :threshold
""")
    suspend fun countLearnedByCourse(
        profileId: Int,
        courseId: Int,
        threshold: Float = 0.85f
    ): Int

    // ─── Stats Bottom Sheet Queries ───────────────────────────────────────────

    /**
     * Words the user has ever interacted with (marked / any progress) in a course.
     * "To Learn" = has a progress record (i.e. queued for learning).
     */
    @Query("""
        SELECT DISTINCT
            w.id            AS wordId,
            w.word          AS word,
            COALESCE(wt.word, 'No translation') AS translation,
            u.name          AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        LEFT JOIN translation tr
            ON (tr.words_id_primary = w.id OR tr.words_id_secondary = w.id)
        LEFT JOIN words wt
            ON (wt.id = tr.words_id_primary OR wt.id = tr.words_id_secondary)
            AND wt.id != w.id
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
        ORDER BY u.name ASC, w.word ASC
    """)
    suspend fun getToLearnWordsByCourse(
        profileId: Int,
        courseId: Int
    ): List<StatWordItem>

    /**
     * Words with knowledge_coeff > practicedThreshold — "Practiced".
     */
    @Query("""
        SELECT DISTINCT
            w.id            AS wordId,
            w.word          AS word,
            COALESCE(wt.word, 'No translation') AS translation,
            u.name          AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        LEFT JOIN translation tr
            ON (tr.words_id_primary = w.id OR tr.words_id_secondary = w.id)
        LEFT JOIN words wt
            ON (wt.id = tr.words_id_primary OR wt.id = tr.words_id_secondary)
            AND wt.id != w.id
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
          AND COALESCE(wp.knowledge_coeff, 0) > :practicedThreshold
        ORDER BY u.name ASC, wp.knowledge_coeff DESC
    """)
    suspend fun getPracticedWordsByCourse(
        profileId: Int,
        courseId: Int,
        practicedThreshold: Float = 0.30f
    ): List<StatWordItem>

    /**
     * Words with knowledge_coeff >= learnedThreshold — "Learned".
     */
    @Query("""
        SELECT DISTINCT
            w.id            AS wordId,
            w.word          AS word,
            COALESCE(wt.word, 'No translation') AS translation,
            u.name          AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        LEFT JOIN translation tr
            ON (tr.words_id_primary = w.id OR tr.words_id_secondary = w.id)
        LEFT JOIN words wt
            ON (wt.id = tr.words_id_primary OR wt.id = tr.words_id_secondary)
            AND wt.id != w.id
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
          AND COALESCE(wp.knowledge_coeff, 0) >= :learnedThreshold
        ORDER BY u.name ASC, wp.knowledge_coeff DESC
    """)
    suspend fun getLearnedWordsByCourse(
        profileId: Int,
        courseId: Int,
        learnedThreshold: Float = 0.85f
    ): List<StatWordItem>
}
