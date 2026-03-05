package com.example.langfire_app.domain.srs

import com.example.langfire_app.data.local.entities.WordProgressEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * SM-2 Spaced Repetition System engine.
 *
 * Algorithm:
 *  - Quality 0 (Forgot):  interval = 1 day, repetition = 0, EF decreases by 0.2 (min 1.3)
 *  - Quality 1 (Hard):    interval = 1 day, repetition kept, EF decreases by 0.14
 *  - Quality 2 (Good):    interval grows, repetition++, EF unchanged
 *  - Quality 3 (Easy):    interval grows faster, repetition++, EF increases
 *
 * knowledge_coeff is mapped smoothly from repetition count so the rest of the app stays aware:
 *   0 rep  → 0.0 (brand new)
 *   1 rep  → 0.2
 *   2 rep  → 0.4
 *   3 rep  → 0.6
 *   4 rep  → 0.75
 *   5+ rep → 0.85 (considered "learned")
 */
object SrsEngine {

    private const val MIN_EASE_FACTOR = 1.3f
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    enum class Quality(val value: Int) {
        FORGOT(0),
        GOOD(2),
        EASY(3)
    }

    data class SrsResult(
        val newInterval: Int,        // days
        val newEaseFactor: Float,
        val newRepetition: Int,
        val nextReviewAt: Long,      // epoch ms
        val newKnowledgeCoeff: Float
    )


    fun processAnswer(
        quality: Quality,
        interval: Int,
        easeFactor: Float,
        repetition: Int,
        nowMs: Long
    ): SrsResult {
        return when (quality) {
            Quality.FORGOT -> {
                val newEF = max(MIN_EASE_FACTOR, easeFactor - 0.2f)
                SrsResult(
                    newInterval = 1,
                    newEaseFactor = newEF,
                    newRepetition = 0,
                    nextReviewAt = nowMs + DAY_MS,
                    newKnowledgeCoeff = 0.0f
                )
            }
            Quality.GOOD -> {
                val newRep = repetition + 1
                val newInterval = when {
                    newRep == 1 -> 1
                    newRep == 2 -> 4
                    else        -> (interval * easeFactor).roundToInt().coerceAtLeast(1)
                }
                SrsResult(
                    newInterval = newInterval,
                    newEaseFactor = easeFactor,
                    newRepetition = newRep,
                    nextReviewAt = nowMs + newInterval * DAY_MS,
                    newKnowledgeCoeff = repetitionToCoeff(newRep)
                )
            }
            Quality.EASY -> {
                val newRep = repetition + 1
                val newEF = min(3.5f, easeFactor + 0.1f)
                val newInterval = when {
                    newRep == 1 -> 1
                    newRep == 2 -> 6
                    else        -> (interval * newEF).roundToInt().coerceAtLeast(1)
                }
                SrsResult(
                    newInterval = newInterval,
                    newEaseFactor = newEF,
                    newRepetition = newRep,
                    nextReviewAt = nowMs + newInterval * DAY_MS,
                    newKnowledgeCoeff = repetitionToCoeff(newRep)
                )
            }
        }
    }

    fun applyResult(entity: WordProgressEntity, result: SrsResult, nowMs: Long): WordProgressEntity {
        val wasCorrect = result.newRepetition > 0
        return entity.copy(
            knowledgeCoeff = result.newKnowledgeCoeff,
            lastReviewed = nowMs,
            correctCount = (entity.correctCount ?: 0) + if (wasCorrect) 1 else 0,
            incorrectCount = (entity.incorrectCount ?: 0) + if (!wasCorrect) 1 else 0,
            srsInterval = result.newInterval,
            srsEaseFactor = result.newEaseFactor,
            srsRepetition = result.newRepetition,
            nextReviewAt = result.nextReviewAt
        )
    }

    private fun repetitionToCoeff(repetition: Int): Float = when {
        repetition <= 0 -> 0.0f
        repetition == 1 -> 0.2f
        repetition == 2 -> 0.4f
        repetition == 3 -> 0.6f
        repetition == 4 -> 0.75f
        else             -> 0.85f
    }
}
