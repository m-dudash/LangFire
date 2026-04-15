package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.SessionWordItem
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.entities.WordProgressEntity
import com.example.langfire_app.domain.model.SessionWord
import com.example.langfire_app.domain.model.LevelProgressStats
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.domain.repository.LearnRepository
import com.example.langfire_app.domain.util.CourseProgressCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearnRepositoryImpl @Inject constructor(
    private val wordProgressDao: WordProgressDao
) : LearnRepository {

    override suspend fun getSessionWords(
        profileId: Int,
        courseId: Int,
        limit: Int
    ): List<SessionWord> = wordProgressDao.getWordsForSession(
        profileId = profileId,
        courseId  = courseId,
        nowMs     = System.currentTimeMillis(),
        limit     = limit
    ).map { it.toDomain() }

    override suspend fun getSessionWordsBounded(
        profileId: Int,
        courseId: Int,
        maxLevelId: Int,
        limit: Int
    ): List<SessionWord> = wordProgressDao.getWordsForSessionByMaxLevel(
        profileId  = profileId,
        courseId    = courseId,
        maxLevelId = maxLevelId,
        nowMs      = System.currentTimeMillis(),
        limit      = limit
    ).map { it.toDomain() }

    override suspend fun getMaxLevelIdForSession(profileId: Int, courseId: Int): Int? {
        val levelStats = wordProgressDao.getWordLevelProgressByCourse(
            profileId = profileId,
            courseId   = courseId,
            threshold = CourseProgressCalculator.LEVEL_MASTERY_THRESHOLD
        ).map { LevelProgressStats(it.levelName, it.totalWords, it.learnedWords) }
        val progress = CourseProgressCalculator.computeCourseProgress(levelStats)

        val targetLevelName = progress.targetLevel ?: return null

        return wordProgressDao.getLevelIdByName(targetLevelName)
    }

    override suspend fun recordAnswer(
        profileId: Int,
        wordId: Int,
        quality: SrsEngine.Quality
    ) {
        val nowMs = System.currentTimeMillis()

        val existing = wordProgressDao.getByWord(profileId, wordId)
        val entity = existing ?: WordProgressEntity(
            id             = 0,
            knowledgeCoeff = 0f,
            lastReviewed   = null,
            correctCount   = 0,
            incorrectCount = 0,
            profileId      = profileId,
            wordId         = wordId,
            srsInterval    = 0,
            srsEaseFactor  = 2.5f,
            srsRepetition  = 0,
            nextReviewAt   = 0L
        )

        val result = SrsEngine.processAnswer(
            quality    = quality,
            intervalMins = entity.srsInterval,
            easeFactor = entity.srsEaseFactor,
            repetition = entity.srsRepetition,
            nowMs      = nowMs
        )

        val wasCorrect = result.newRepetition > 0
        val updated = entity.copy(
            knowledgeCoeff = result.newKnowledgeCoeff,
            lastReviewed = nowMs,
            correctCount = (entity.correctCount ?: 0) + if (wasCorrect) 1 else 0,
            incorrectCount = (entity.incorrectCount ?: 0) + if (!wasCorrect) 1 else 0,
            srsInterval = result.newIntervalMins,
            srsEaseFactor = result.newEaseFactor,
            srsRepetition = result.newRepetition,
            nextReviewAt = result.nextReviewAt
        )
        if (existing == null) {
            wordProgressDao.insert(updated)
        } else {
            wordProgressDao.update(updated)
        }
    }

    override suspend fun getDistractors(
        profileId: Int,
        courseId: Int,
        excludedWordId: Int,
        limit: Int
    ): List<SessionWord> = wordProgressDao
        .getRandomWords(profileId, courseId, excludedWordId, limit)
        .map { it.toDomain() }


    private fun SessionWordItem.toDomain() = SessionWord(
        wordId          = wordId,
        word            = word,
        translation     = translation,
        article         = article,
        gender          = gender,
        plural          = plural,
        wordType        = wordType,
        exampleSentence = exampleSentence,
        knowledgeCoeff  = knowledgeCoeff,
        nextReviewAt    = nextReviewAt,
        srsInterval     = srsInterval,
        srsEaseFactor   = srsEaseFactor,
        srsRepetition   = srsRepetition,
        correctCount    = correctCount,
        incorrectCount  = incorrectCount,
        audioPath       = audioPath
    )
}
