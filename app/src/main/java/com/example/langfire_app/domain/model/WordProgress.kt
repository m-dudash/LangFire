package com.example.langfire_app.domain.model

/**
 * Domain model representing the learning progress for a specific word.
 * Contains the knowledge coefficient used for spaced repetition
 * and statistics used by the Gamification Engine.
 */
data class WordProgress(
    val id: Int = 0,
    val knowledgeCoeff: Float? = null,
    val lastReviewed: Long? = null,
    val correctCount: Int? = null,
    val incorrectCount: Int? = null,
    val profileId: Int,
    val wordId: Int
)
