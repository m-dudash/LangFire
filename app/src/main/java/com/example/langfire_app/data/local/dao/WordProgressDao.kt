package com.example.langfire_app.data.local.dao

import androidx.room.*
import com.example.langfire_app.data.local.entities.WordLevelProgress
import com.example.langfire_app.data.local.entities.WordProgressEntity


data class StatWordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val unitName: String,
    val knowledgeCoeff: Float?,
    @ColumnInfo(name = "srs_repetition") val srsRepetition: Int?,
    @ColumnInfo(name = "next_review_at") val nextReviewAt: Long?,
    val audioPath: String?
)


data class SessionWordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val article: String?,
    val gender: String?,
    val plural: String?,
    val wordType: String?,
    val exampleSentence: String?,
    val knowledgeCoeff: Float?,
    val nextReviewAt: Long?,
    val srsInterval: Int?,
    val srsEaseFactor: Float?,
    val srsRepetition: Int?,
    val correctCount: Int?,
    val incorrectCount: Int?,
    val audioPath: String?
)

@Dao
interface WordProgressDao {

    @Query("SELECT id FROM level WHERE name = :levelName LIMIT 1")
    suspend fun getLevelIdByName(levelName: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: WordProgressEntity): Long

    @Query("SELECT * FROM word_progress WHERE profile_id = :profileId")
    suspend fun getAllByProfileId(profileId: Int): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE profile_id = :profileId AND word_id = :wordId")
    suspend fun getByWord(profileId: Int, wordId: Int): WordProgressEntity?

    @Query("SELECT COUNT(*) FROM word_progress WHERE profile_id = :profileId AND knowledge_coeff >= :threshold AND (srs_repetition IS NULL OR srs_repetition != -1)")
    suspend fun countLearnedWords(profileId: Int, threshold: Float): Int

    @Query("SELECT SUM(correct_count) FROM word_progress WHERE profile_id = :profileId AND (srs_repetition IS NULL OR srs_repetition != -1)")
    suspend fun getTotalCorrectCount(profileId: Int): Int?

    @Query("SELECT SUM(incorrect_count) FROM word_progress WHERE profile_id = :profileId AND (srs_repetition IS NULL OR srs_repetition != -1)")
    suspend fun getTotalIncorrectCount(profileId: Int): Int?

    @Update
    suspend fun update(progress: WordProgressEntity)

    @Query("DELETE FROM word_progress WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM word_progress WHERE profile_id = :profileId")
    suspend fun deleteAllByProfileId(profileId: Int)


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


    @Query("""
        SELECT
            l.name AS levelName,
            COUNT(DISTINCT w.id) AS totalWords,
            COUNT(DISTINCT CASE
                WHEN wp.profile_id = :profileId
                 AND wp.knowledge_coeff >= :threshold
                THEN w.id END) AS learnedWords
        FROM level l
        LEFT JOIN words  w  ON w.level_id  = l.id
        LEFT JOIN word_progress wp ON wp.word_id = w.id
        GROUP BY l.id, l.name
        ORDER BY l.name ASC
    """)
    suspend fun getWordLevelProgress(
        profileId: Int,
        threshold: Float
    ): List<WordLevelProgress>


    @Query("""
        SELECT
            l.name AS levelName,
            COUNT(DISTINCT CASE
                WHEN u.course_id = :courseId
                 AND w.language_id = c.target_language_id
                THEN w.id END) AS totalWords,
            COUNT(DISTINCT CASE
                WHEN u.course_id = :courseId
                 AND w.language_id = c.target_language_id
                 AND wp.profile_id = :profileId
                 AND wp.knowledge_coeff >= :threshold
                THEN w.id END) AS learnedWords
        FROM level l
        LEFT JOIN words  w  ON w.level_id  = l.id
        LEFT JOIN unit   u  ON u.id = w.unit_id
        LEFT JOIN course c  ON u.course_id = c.id
        LEFT JOIN word_progress wp ON wp.word_id = w.id
        GROUP BY l.id, l.name
        ORDER BY l.name ASC
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
    JOIN course c ON u.course_id = c.id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
      AND (
          (wp.srs_repetition IS NULL OR wp.srs_repetition = 0)
          OR wp.next_review_at <= :nowMs
      )
      AND (wp.knowledge_coeff IS NULL OR wp.knowledge_coeff < 0.95)
      AND w.language_id = c.target_language_id
""")
    suspend fun countToLearnByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long
    ): Int

    @Query("""
    SELECT COUNT(DISTINCT wp.word_id)
    FROM word_progress wp
    JOIN words w ON w.id = wp.word_id
    JOIN unit u ON u.id = w.unit_id
    JOIN course c ON u.course_id = c.id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
      AND wp.srs_repetition >= :minRepetitionThreshold
      AND wp.srs_repetition < :maxRepetitionThreshold
      AND (wp.next_review_at IS NULL OR wp.next_review_at > :nowMs)
      AND w.language_id = c.target_language_id
""")
    suspend fun countPracticedByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long,
        minRepetitionThreshold: Int = 1,
        maxRepetitionThreshold: Int = 4
    ): Int

    @Query("""
    SELECT COUNT(DISTINCT wp.word_id)
    FROM word_progress wp
    JOIN words w ON w.id = wp.word_id
    JOIN unit u ON u.id = w.unit_id
    JOIN course c ON u.course_id = c.id
    WHERE wp.profile_id = :profileId
      AND u.course_id = :courseId
      AND wp.srs_repetition >= :repetitionThreshold
      AND (wp.next_review_at IS NULL OR wp.next_review_at > :nowMs)
      AND w.language_id = c.target_language_id
""")
    suspend fun countLearnedByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long,
        repetitionThreshold: Int = 4
    ): Int


    @Query("""
        SELECT DISTINCT
            w.id AS wordId,
            w.word AS word,
            COALESCE(wt_p.word, wt_s.word, 'No translation') AS translation,
            u.name AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff,
            wp.srs_repetition  AS srs_repetition, wp.next_review_at  AS next_review_at, w.audio_path AS audioPath
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
          AND w.language_id = c.target_language_id
          AND (
              (wp.srs_repetition IS NULL OR wp.srs_repetition = 0)
              OR wp.next_review_at <= :nowMs
          )
          AND (wp.knowledge_coeff IS NULL OR wp.knowledge_coeff < 0.95)
        ORDER BY u.name ASC, w.word ASC
    """)
    suspend fun getToLearnWordsByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long
    ): List<StatWordItem>


    @Query("""
        SELECT DISTINCT
            w.id AS wordId,
            w.word AS word,
            COALESCE(wt_p.word, wt_s.word, 'No translation') AS translation,
            u.name AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff,
            wp.srs_repetition AS srs_repetition,
            wp.next_review_at AS next_review_at,
            w.audio_path AS audioPath
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
          AND w.language_id = c.target_language_id
          AND wp.srs_repetition >= :minRepetitionThreshold
          AND wp.srs_repetition < :maxRepetitionThreshold
          AND (wp.next_review_at IS NULL OR wp.next_review_at > :nowMs)
        ORDER BY u.name ASC, wp.srs_repetition DESC
    """)
    suspend fun getPracticedWordsByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long,
        minRepetitionThreshold: Int = 1,
        maxRepetitionThreshold: Int = 4
    ): List<StatWordItem>


    @Query("""
        SELECT DISTINCT
            w.id AS wordId,
            w.word  AS word,
            COALESCE(wt_p.word, wt_s.word, 'No translation') AS translation,
            u.name AS unitName,
            wp.knowledge_coeff AS knowledgeCoeff,
            wp.srs_repetition AS srs_repetition,
            wp.next_review_at AS next_review_at,
            w.audio_path AS audioPath
        FROM word_progress wp
        JOIN words w ON w.id = wp.word_id
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        WHERE wp.profile_id = :profileId
          AND u.course_id   = :courseId
          AND w.language_id = c.target_language_id
          AND wp.srs_repetition >= :repetitionThreshold
          AND (wp.next_review_at IS NULL OR wp.next_review_at > :nowMs)
        ORDER BY u.name ASC, wp.srs_repetition DESC
    """)
    suspend fun getLearnedWordsByCourse(
        profileId: Int,
        courseId: Int,
        nowMs: Long,
        repetitionThreshold: Int = 4
    ): List<StatWordItem>


    @Query("""
        SELECT
            w.id                AS wordId,
            w.word              AS word,
            COALESCE(wt_p.word, wt_s.word, '') AS translation,
            a.name              AS article,
            g.name              AS gender,
            w.plural            AS plural,
            wType.type          AS wordType,
            COALESCE(tr_p.example_sentence, tr_s.example_sentence) AS exampleSentence,
            wp.knowledge_coeff  AS knowledgeCoeff,
            wp.next_review_at   AS nextReviewAt,
            wp.srs_interval     AS srsInterval,
            wp.srs_ease_factor  AS srsEaseFactor,
            wp.srs_repetition   AS srsRepetition,
            wp.correct_count    AS correctCount,
            wp.incorrect_count  AS incorrectCount,
            w.audio_path        AS audioPath
        FROM words w
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        LEFT JOIN article a ON a.id = w.article_id
        LEFT JOIN gender g ON g.id = w.gender_id
        LEFT JOIN word_type wType ON wType.id = w.word_type_id
        LEFT JOIN word_progress wp
            ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
          AND w.language_id = c.target_language_id
          AND (
                (wp.knowledge_coeff IS NOT NULL AND wp.knowledge_coeff < 0.95 AND (wp.next_review_at IS NULL OR wp.next_review_at <= :nowMs))
             OR
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
            COALESCE(wt_p.word, wt_s.word, '') AS translation,
            a.name              AS article,
            g.name              AS gender,
            w.plural            AS plural,
            wType.type          AS wordType,
            COALESCE(tr_p.example_sentence, tr_s.example_sentence) AS exampleSentence,
            wp.knowledge_coeff  AS knowledgeCoeff,
            wp.next_review_at   AS nextReviewAt,
            wp.srs_interval     AS srsInterval,
            wp.srs_ease_factor  AS srsEaseFactor,
            wp.srs_repetition   AS srsRepetition,
            wp.correct_count    AS correctCount,
            wp.incorrect_count  AS incorrectCount,
            w.audio_path        AS audioPath
        FROM words w
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        LEFT JOIN article a ON a.id = w.article_id
        LEFT JOIN gender g ON g.id = w.gender_id
        LEFT JOIN word_type wType ON wType.id = w.word_type_id
        LEFT JOIN word_progress wp
            ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
          AND w.language_id = c.target_language_id
          AND w.level_id <= :maxLevelId
          AND (
                (wp.knowledge_coeff IS NOT NULL AND wp.knowledge_coeff < 0.95 AND (wp.next_review_at IS NULL OR wp.next_review_at <= :nowMs))
             OR
                (wp.knowledge_coeff IS NOT NULL AND wp.knowledge_coeff = 0.0 AND wp.srs_repetition = 0)
          )
        GROUP BY w.id
        ORDER BY
            CASE WHEN wp.srs_repetition IS NULL OR wp.srs_repetition = 0 THEN 0 ELSE 1 END ASC,
            (:nowMs - COALESCE(wp.next_review_at, 0)) DESC
        LIMIT :limit
    """)
    suspend fun getWordsForSessionByMaxLevel(
        profileId: Int,
        courseId: Int,
        maxLevelId: Int,
        nowMs: Long,
        limit: Int = 20
    ): List<SessionWordItem>

    @Query("""
        SELECT
            w.id                AS wordId,
            w.word              AS word,
            COALESCE(wt_p.word, wt_s.word, '') AS translation,
            a.name              AS article,
            g.name              AS gender,
            w.plural            AS plural,
            wType.type          AS wordType,
            COALESCE(tr_p.example_sentence, tr_s.example_sentence) AS exampleSentence,
            wp.knowledge_coeff  AS knowledgeCoeff,
            wp.next_review_at   AS nextReviewAt,
            wp.srs_interval     AS srsInterval,
            wp.srs_ease_factor  AS srsEaseFactor,
            wp.srs_repetition   AS srsRepetition,
            wp.correct_count    AS correctCount,
            wp.incorrect_count  AS incorrectCount,
            w.audio_path        AS audioPath
        FROM words w
        JOIN unit u ON u.id = w.unit_id
        JOIN course c ON u.course_id = c.id
        LEFT JOIN translation tr_p ON tr_p.words_id_primary = w.id
        LEFT JOIN translation tr_s ON tr_s.words_id_secondary = w.id
        LEFT JOIN words wt_p ON wt_p.id = tr_p.words_id_secondary
        LEFT JOIN words wt_s ON wt_s.id = tr_s.words_id_primary
        LEFT JOIN article a ON a.id = w.article_id
        LEFT JOIN gender g ON g.id = w.gender_id
        LEFT JOIN word_type wType ON wType.id = w.word_type_id
        LEFT JOIN word_progress wp
            ON wp.word_id = w.id AND wp.profile_id = :profileId
        WHERE u.course_id = :courseId
          AND w.language_id = c.target_language_id
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
