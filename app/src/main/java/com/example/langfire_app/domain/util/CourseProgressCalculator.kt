package com.example.langfire_app.domain.util

import com.example.langfire_app.domain.model.LevelProgressStats

object CourseProgressCalculator {

    const val LEVEL_MASTERY_THRESHOLD = 0.93f

    data class CourseLevelProgress(
        val achievedLevel: String?,
        val targetLevel: String?,
        val wordsLearnedInTarget: Int,
        val totalWordsInTarget: Int
    )

    fun computeCourseProgress(
        levelStats: List<LevelProgressStats>,
        masteryRatio: Float = LEVEL_MASTERY_THRESHOLD
    ): CourseLevelProgress {
        var achievedLevel: String? = null
        var targetStat: LevelProgressStats? = null

        for (stat in levelStats) {
            val ratio = if (stat.totalWords > 0)
                stat.learnedWords.toFloat() / stat.totalWords
            else
                0f

            if (ratio >= masteryRatio && stat.totalWords > 0) {
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
