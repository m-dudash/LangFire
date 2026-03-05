package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.SessionWordItem
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.entities.WordProgressEntity
import com.example.langfire_app.domain.srs.SrsEngine
import com.example.langfire_app.domain.repository.LearnRepository
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
    ): List<SessionWordItem> = wordProgressDao.getWordsForSession(
        profileId = profileId,
        courseId  = courseId,
        nowMs     = System.currentTimeMillis(),
        limit     = limit
    )

    override suspend fun recordAnswer(
        profileId: Int,
        wordId: Int,
        quality: SrsEngine.Quality
    ) {
        val nowMs = System.currentTimeMillis()

        // Load or create the existing progress record
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

        // Compute SM-2 result
        val result = SrsEngine.processAnswer(
            quality    = quality,
            interval   = entity.srsInterval,
            easeFactor = entity.srsEaseFactor,
            repetition = entity.srsRepetition,
            nowMs      = nowMs
        )

        // Apply and save
        val updated = SrsEngine.applyResult(entity, result, nowMs)
        if (existing == null) {
            wordProgressDao.insert(updated)
        } else {
            wordProgressDao.update(updated)
        }
    }
}
