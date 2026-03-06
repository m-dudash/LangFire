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

/**
 * A word item loaded for an SRS learning session.
 * Contains all fields needed to show the flashcard and update SM-2 state.
 */
data class SessionWordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val article: String?,
    val plural: String?,
    val wordType: String?,
    val exampleSentence: String?,
    val knowledgeCoeff: Float?,
    val nextReviewAt: Long?,
    val srsInterval: Int?,
    val srsEaseFactor: Float?,
    val srsRepetition: Int?,
    val correctCount: Int?,
    val incorrectCount: Int?
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

    // ─── SRS Session Query ────────────────────────────────────────────────────

    /**
     * Selects words for a learning session using SRS scheduling.
     *
     * Priority order:
     *  1. Completely new words (no progress record) – so the user always sees fresh vocab
     *  2. Overdue words (nextReviewAt <= now) ordered by how overdue they are (most overdue first)
     *  3. Words due today
     *
     * Words with knowledge_coeff >= 0.95 (fully learned) are excluded from new sessions
     * but still show up if they are overdue (to maintain long-term retention).
     */
    @Query("""
        SELECT
            w.id                AS wordId,
            w.word              AS word,
            COALESCE(wt.word, '') AS translation,
            a.name              AS article,
            w.plural            AS plural,
            wType.type          AS wordType,
            tr.example_sentence AS exampleSentence,
            wp.knowledge_coeff  AS knowledgeCoeff,
            wp.next_review_at   AS nextReviewAt,
            wp.srs_interval     AS srsInterval,
            wp.srs_ease_factor  AS srsEaseFactor,
            wp.srs_repetition   AS srsRepetition,
            wp.correct_count    AS correctCount,
            wp.incorrect_count  AS incorrectCount
        FROM words w
        JOIN unit u ON u.id = w.unit_id
        LEFT JOIN translation tr
            ON (tr.words_id_primary = w.id OR tr.words_id_secondary = w.id)
        LEFT JOIN words wt
            ON (wt.id = tr.words_id_primary OR wt.id = tr.words_id_secondary)
            AND wt.id != w.id
        LEFT JOIN article a ON a.id = w.article_id
        LEFT JOIN word_type wType ON wType.id = w.word_type_id
        LEFT JOIN word_progress wp
            ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
          AND (
                -- New word: profile has it marked for learning (coeff = 0.0) or there's a progress record that is due
                (wp.knowledge_coeff IS NOT NULL AND wp.knowledge_coeff < 0.95 AND (wp.next_review_at IS NULL OR wp.next_review_at <= :nowMs))
             OR
                -- Word explicitly queued (coeff = 0) that hasn't been reviewed yet
                (wp.knowledge_coeff IS NOT NULL AND wp.knowledge_coeff = 0.0 AND wp.srs_repetition = 0)
          )
        GROUP BY w.id
        ORDER BY
            CASE WHEN wp.srs_repetition IS NULL OR wp.srs_repetition = 0 THEN 0 ELSE 1 END ASC,
            (:nowMs - COALESCE(wp.next_review_at, 0)) DESC
        LIMIT :limit
    """)
    suspend fun getWordsForSession(
        profileId: Int,
        courseId: Int,
        nowMs: Long,
        limit: Int = 20
    ): List<SessionWordItem>

    @Query("""
        SELECT
            w.id                AS wordId,
            w.word              AS word,
            COALESCE(wt.word, '') AS translation,
            a.name              AS article,
            w.plural            AS plural,
            wType.type          AS wordType,
            tr.example_sentence AS exampleSentence,
            wp.knowledge_coeff  AS knowledgeCoeff,
            wp.next_review_at   AS nextReviewAt,
            wp.srs_interval     AS srsInterval,
            wp.srs_ease_factor  AS srsEaseFactor,
            wp.srs_repetition   AS srsRepetition,
            wp.correct_count    AS correctCount,
            wp.incorrect_count  AS incorrectCount
        FROM words w
        JOIN unit u ON u.id = w.unit_id
        LEFT JOIN translation tr
            ON (tr.words_id_primary = w.id OR tr.words_id_secondary = w.id)
        LEFT JOIN words wt
            ON (wt.id = tr.words_id_primary OR wt.id = tr.words_id_secondary)
            AND wt.id != w.id
        LEFT JOIN article a ON a.id = w.article_id
        LEFT JOIN word_type wType ON wType.id = w.word_type_id
        LEFT JOIN word_progress wp
            ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
          AND w.id != :excludedWordId
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomWords(
        profileId: Int,
        courseId: Int,
        excludedWordId: Int,
        limit: Int
    ): List<SessionWordItem>
}
