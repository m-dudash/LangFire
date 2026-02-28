package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.CourseLevelInfo
import com.example.langfire_app.domain.model.HomeCourseStats
import com.example.langfire_app.domain.model.ProfileStats
import com.example.langfire_app.domain.repository.StatsRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val wordProgressDao: WordProgressDao,
    private val courseDao: CourseDao
) : StatsRepository {

    override suspend fun getProfileStats(profileId: Int): ProfileStats {
        val wordsLearned = wordProgressDao.countLearnedWords(
            profileId  = profileId,
            threshold  = LEARNED_THRESHOLD
        )
        val totalCorrect = wordProgressDao.getTotalCorrectCount(profileId) ?: 0
        val totalErrors  = wordProgressDao.getTotalIncorrectCount(profileId) ?: 0
        val toughestWord = wordProgressDao.getToughestWordText(profileId)


        // ── CEFR progression for all courses ───────────────────────────
        val courseProgress: List<CourseLevelInfo> = courseDao.getAll()
            .mapNotNull { course ->
                val levelStats = wordProgressDao.getWordLevelProgressByCourse(
                    profileId = profileId,
                    courseId = course.id,
                    threshold = LEARNED_THRESHOLD
                )
                if (levelStats.none { it.totalWords > 0 }) return@mapNotNull null

                val progress = GamificationEngine.computeCourseProgress(levelStats)
                CourseLevelInfo(
                    courseName           = course.name,
                    courseIcon           = course.icon,
                    targetLang           = course.targetLang,
                    achievedLevel        = progress.achievedLevel,
                    targetLevel          = progress.targetLevel,
                    wordsLearnedInTarget = progress.wordsLearnedInTarget,
                    totalWordsInTarget   = progress.totalWordsInTarget
                )
            }

        return ProfileStats(
            wordsLearned   = wordsLearned,
            totalCorrect   = totalCorrect,
            totalErrors    = totalErrors,
            toughestWord   = toughestWord,
            courseProgress = courseProgress
        )
    }

    companion object {
        const val LEARNED_THRESHOLD = 0.8f
    }

    override suspend fun getHomeCourseStats(profileId: Int, courseId: Int): HomeCourseStats {
        val toLearn = wordProgressDao.countToLearnByCourse(profileId, courseId)
        val practiced = wordProgressDao.countPracticedByCourse(
            profileId = profileId,
            courseId = courseId,
            threshold = 0.30f
        )
        val learned = wordProgressDao.countLearnedByCourse(
            profileId = profileId,
            courseId = courseId,
            threshold = 0.85f
        )

        return HomeCourseStats(
            toLearn = toLearn,
            practiced = practiced,
            learned = learned
        )
    }
}
