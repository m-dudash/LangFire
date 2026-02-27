package com.example.langfire_app.data.local.entities

/**
 * Room query-result holder for per-CEFR-level word progress.
 *
 * Not a @Entity — this is a projection used only for the
 * [com.example.langfire_app.data.local.dao.WordProgressDao.getWordLevelProgress] query.
 *
 * @param levelName   CEFR level string (e.g. "A1", "B2") from the `level` table.
 * @param totalWords  Total number of words that belong to this level in the course.
 * @param learnedWords Words the profile has mastered (knowledge_coeff ≥ threshold).
 */
data class WordLevelProgress(
    val levelName: String,
    val totalWords: Int,
    val learnedWords: Int
)
