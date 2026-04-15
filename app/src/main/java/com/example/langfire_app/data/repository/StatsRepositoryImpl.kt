package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.dao.StatWordItem
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.domain.util.CourseProgressCalculator
import com.example.langfire_app.domain.model.CourseLevelInfo
import com.example.langfire_app.domain.model.HomeCourseStats
import com.example.langfire_app.domain.model.LevelProgressStats
import com.example.langfire_app.domain.model.ProfileStats
import com.example.langfire_app.domain.model.StatWord
import com.example.langfire_app.domain.repository.BehaviorRepository
import com.example.langfire_app.domain.repository.StatsRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val wordProgressDao: WordProgressDao,
    private val courseDao: CourseDao,
    private val behaviorRepository: BehaviorRepository
) : StatsRepository {

    override suspend fun getProfileStats(profileId: Int): ProfileStats {
        val wordsLearned = wordProgressDao.countLearnedWords(
            profileId  = profileId,
            threshold  = LEARNED_THRESHOLD
        )
        val totalCorrect = wordProgressDao.getTotalCorrectCount(profileId) ?: 0
        val totalErrors  = wordProgressDao.getTotalIncorrectCount(profileId) ?: 0
        val toughestWord = wordProgressDao.getToughestWordText(profileId)

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStartMs = cal.timeInMillis

        val scCount = behaviorRepository.getBehaviorsByTypeAfter(
            profileId, "session_complete", todayStartMs
        ).sumOf { b -> 
            if (b.attributes["cumulative_already_tracked"] == "true") 0
            else b.attributes["correct_count"]?.toIntOrNull() ?: 0 
        }

        val caCount = behaviorRepository.getBehaviorsByTypeAfter(
            profileId, "correct_answer", todayStartMs
        ).size

        val correctToday = scCount + caCount


        val courseProgress: List<CourseLevelInfo> = courseDao.getAll()
            .mapNotNull { course ->
                val levelStats = wordProgressDao.getWordLevelProgressByCourse(
                    profileId = profileId,
                    courseId = course.id,
                    threshold = LEARNED_THRESHOLD
                ).map { LevelProgressStats(it.levelName, it.totalWords, it.learnedWords) }
                if (levelStats.none { it.totalWords > 0 }) return@mapNotNull null

                val progress = CourseProgressCalculator.computeCourseProgress(levelStats)
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
            correctToday   = correctToday,
            toughestWord   = toughestWord,
            courseProgress = courseProgress
        )
    }

    companion object {
        const val LEARNED_THRESHOLD = 0.8f
    }

    override suspend fun getHomeCourseStats(profileId: Int, courseId: Int): HomeCourseStats {
        val nowMs = System.currentTimeMillis()
        val toLearn = wordProgressDao.countToLearnByCourse(profileId, courseId, nowMs)
        val practiced = wordProgressDao.countPracticedByCourse(
            profileId = profileId,
            courseId = courseId,
            nowMs = nowMs
        )
        val learned = wordProgressDao.countLearnedByCourse(
            profileId = profileId,
            courseId = courseId,
            nowMs = nowMs
        )

        return HomeCourseStats(
            toLearn = toLearn,
            practiced = practiced,
            learned = learned
        )
    }

    override suspend fun getToLearnWords(profileId: Int, courseId: Int): List<StatWord> =
        wordProgressDao.getToLearnWordsByCourse(profileId, courseId, System.currentTimeMillis())
            .map { it.toDomain() }

    override suspend fun getPracticedWords(profileId: Int, courseId: Int): List<StatWord> =
        wordProgressDao.getPracticedWordsByCourse(profileId, courseId, System.currentTimeMillis())
            .map { it.toDomain() }

    override suspend fun getLearnedWords(profileId: Int, courseId: Int): List<StatWord> =
        wordProgressDao.getLearnedWordsByCourse(profileId, courseId, System.currentTimeMillis())
            .map { it.toDomain() }


    private fun StatWordItem.toDomain() = StatWord(
        wordId        = wordId,
        word          = word,
        translation   = translation,
        unitName      = unitName,
        knowledgeCoeff = knowledgeCoeff,
        srsRepetition = srsRepetition,
        nextReviewAt  = nextReviewAt,
        audioPath     = audioPath
    )
}
