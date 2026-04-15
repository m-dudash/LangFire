package com.example.langfire_app.domain.srs

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object SrsEngine {

    private const val MIN_EASE_FACTOR = 1.3f
    private const val MINUTE_MS = 60L * 1000L
    private const val HOUR_MS = 60L * MINUTE_MS
    private const val DAY_MS = 24L * HOUR_MS

    enum class Quality(val value: Int) {
        FORGOT(0),
        GOOD(2),
        EASY(3)
    }

    data class SrsResult(
        val newIntervalMins: Int,
        val newEaseFactor: Float,
        val newRepetition: Int,
        val nextReviewAt: Long,
        val newKnowledgeCoeff: Float
    )

    fun processAnswer(
        quality: Quality, intervalMins: Int, easeFactor: Float, repetition: Int, nowMs: Long
    ): SrsResult {
        return when (quality) {
            Quality.FORGOT -> {
                val newEF = max(MIN_EASE_FACTOR, easeFactor - 0.2f)
                val newIntervalMins = 10
                SrsResult(newIntervalMins = newIntervalMins, newEaseFactor = newEF, newRepetition = 0,
                    nextReviewAt = nowMs + newIntervalMins * MINUTE_MS, newKnowledgeCoeff = 0.0f)
            }
            Quality.GOOD -> {
                val newRep = repetition + 1
                val newIntervalMins = when {
                    newRep == 1 -> 4 * 60
                    newRep == 2 -> 24 * 60
                    newRep == 3 -> 3 * 24 * 60
                    else -> (intervalMins * easeFactor).roundToInt().coerceAtLeast(1)
                }
                SrsResult(newIntervalMins = newIntervalMins, newEaseFactor = easeFactor,
                    newRepetition = newRep, nextReviewAt = nowMs + newIntervalMins * MINUTE_MS,
                    newKnowledgeCoeff = repetitionToCoeff(newRep))
            }
            Quality.EASY -> {
                val newRep = repetition + 1
                val newEF = min(3.5f, easeFactor + 0.1f)
                val newIntervalMins = when {
                    newRep == 1 -> 24 * 60
                    newRep == 2 -> 4 * 24 * 60
                    else -> (intervalMins * newEF).roundToInt().coerceAtLeast(1)
                }
                SrsResult(newIntervalMins = newIntervalMins, newEaseFactor = newEF,
                    newRepetition = newRep, nextReviewAt = nowMs + newIntervalMins * MINUTE_MS,
                    newKnowledgeCoeff = repetitionToCoeff(newRep))
            }
        }
    }


    private fun repetitionToCoeff(repetition: Int): Float = when {
        repetition <= 0 -> 0.0f
        repetition == 1 -> 0.2f
        repetition == 2 -> 0.4f
        repetition == 3 -> 0.6f
        repetition == 4 -> 0.80f
        else             -> 1f
    }
}
