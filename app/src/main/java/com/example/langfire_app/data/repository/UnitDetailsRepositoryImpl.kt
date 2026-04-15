package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.UnitDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.dao.WordsDao
import com.example.langfire_app.data.local.entities.WordProgressEntity
import com.example.langfire_app.domain.model.UnitWordItem
import com.example.langfire_app.domain.repository.UnitDetailsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitDetailsRepositoryImpl @Inject constructor(
    private val unitDao: UnitDao,
    private val wordsDao: WordsDao,
    private val wordProgressDao: WordProgressDao
) : UnitDetailsRepository {

    override suspend fun getUnitName(unitId: Int): String? =
        unitDao.getUnitById(unitId)?.name

    override suspend fun getWordsForUnit(unitId: Int, profileId: Int): List<UnitWordItem> =
        wordsDao.getWordsForUnit(unitId, profileId).map { entity ->
            UnitWordItem(
                wordId = entity.wordId,
                word = entity.word,
                translation = entity.translation,
                knowledgeCoeff = entity.knowledgeCoeff,
                audioPath = entity.audioPath
            )
        }

    override suspend fun countToLearnByCourse(profileId: Int, courseId: Int, nowMs: Long): Int =
        wordProgressDao.countToLearnByCourse(profileId, courseId, nowMs)

    override suspend fun saveWordMark(profileId: Int, wordId: Int, coeff: Float) {
        val existing = wordProgressDao.getByWord(profileId, wordId)
        val updated = existing?.copy(
            knowledgeCoeff = coeff,
            lastReviewed = System.currentTimeMillis(),
            srsRepetition = if (coeff == 1f) -1 else (existing.srsRepetition.takeIf { coeff != 0f } ?: 0),
            srsInterval   = if (coeff == 1f) 0  else (existing.srsInterval.takeIf   { coeff != 0f } ?: 0),
            nextReviewAt  = if (coeff == 1f) 0L else (existing.nextReviewAt.takeIf  { coeff != 0f } ?: 0L)
        ) ?: WordProgressEntity(
            id             = 0,
            knowledgeCoeff = coeff,
            lastReviewed   = System.currentTimeMillis(),
            correctCount   = 0,
            incorrectCount = 0,
            profileId      = profileId,
            wordId         = wordId,
            srsRepetition  = if (coeff == 1f) -1 else 0,
            srsInterval    = 0,
            nextReviewAt   = 0L
        )

        if (existing == null) {
            wordProgressDao.insert(updated)
        } else {
            wordProgressDao.update(updated)
        }
    }

    override suspend fun clearWordProgress(profileId: Int, wordId: Int) {
        val existing = wordProgressDao.getByWord(profileId, wordId)
        if (existing != null) {
            wordProgressDao.deleteById(existing.id)
        }
    }
}
