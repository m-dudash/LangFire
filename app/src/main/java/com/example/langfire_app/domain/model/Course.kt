package com.example.langfire_app.domain.model

data class Course(
    val id: Int = 0,
    val name: String,
    val targetLang: String,
    val icon: String
)

/**
 * Represents a user's full progression state in a specific language course.
 *
 * Built by [com.example.langfire_app.domain.engine.GamificationEngine.computeCourseProgress]
 * and assembled with course metadata in StatsRepositoryImpl.
 *
 * @param courseName             Human-readable course name (e.g. "Dutch (from English)")
 * @param courseIcon             Emoji flag icon
 * @param targetLang             ISO language code (e.g. "nl")
 * @param achievedLevel          Highest CEFR level fully completed (≥80 % mastery).
 *                               Null when the user hasn't completed any level yet.
 * @param targetLevel            CEFR level currently being worked towards.
 *                               Null when the user has mastered all levels (C2 done).
 * @param wordsLearnedInTarget   Words mastered so far inside [targetLevel].
 * @param totalWordsInTarget     Total words that belong to [targetLevel].
 */
data class CourseLevelInfo(
    val courseName: String,
    val courseIcon: String,
    val targetLang: String,
    val achievedLevel: String?,
    val targetLevel: String?,
    val wordsLearnedInTarget: Int,
    val totalWordsInTarget: Int
)