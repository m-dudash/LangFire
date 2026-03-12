package com.example.langfire_app.domain.util

import com.example.langfire_app.data.local.entities.WordLevelProgress

/**
 * Pure utility that computes course-level (CEFR) progression from raw word stats.
 *
 * This lives outside the Gamification Engine so that any layer
 * (repositories, view models) can use it without pulling in the engine.
 */
object CourseProgressCalculator {

    /** Default mastery threshold – a level is "completed" at 80 %. */
    const val LEVEL_MASTERY_THRESHOLD = 0.8f

    /**
     * Typed result of [computeCourseProgress].
     *
     * @param achievedLevel          Highest CEFR level fully completed; null = nothing yet.
     * @param targetLevel            Next level to work towards; null = C2 already mastered.
     * @param wordsLearnedInTarget   Words mastered in [targetLevel].
     * @param totalWordsInTarget     Total words in [targetLevel].
     */
    data class CourseLevelProgress(
        val achievedLevel: String?,
        val targetLevel: String?,
        val wordsLearnedInTarget: Int,
        val totalWordsInTarget: Int
    )

    /**
     * Computes the full progression state for a course from raw per-level word stats.
     *
     * Algorithm (levels must be ordered A1 → C2 by the DAO query):
     *  1. Walk levels in ascending order.
     *  2. A level is *completed* when learnedWords / totalWords ≥ [masteryRatio].
     *  3. [achievedLevel] = highest consecutive completed level.
     *  4. [targetLevel]   = first non-empty level that is NOT yet completed.
     *  5. If all levels are completed, [targetLevel] is null (mastery reached).
     *  6. If no level is completed yet, [achievedLevel] is null.
     */
    fun computeCourseProgress(
        levelStats: List<WordLevelProgress>,
        masteryRatio: Float = LEVEL_MASTERY_THRESHOLD
    ): CourseLevelProgress {
        var achievedLevel: String? = null
        var targetStat: WordLevelProgress? = null

        for (stat in levelStats) {
            if (stat.totalWords == 0) continue
            val ratio = stat.learnedWords.toFloat() / stat.totalWords
            if (ratio >= masteryRatio) {
                achievedLevel = stat.levelName
            } else {
                targetStat = stat
                break
            }
        }

        return CourseLevelProgress(
            achievedLevel        = achievedLevel,
            targetLevel          = targetStat?.levelName,
            wordsLearnedInTarget = targetStat?.learnedWords ?: 0,
            totalWordsInTarget   = targetStat?.totalWords   ?: 0
        )
    }
}
