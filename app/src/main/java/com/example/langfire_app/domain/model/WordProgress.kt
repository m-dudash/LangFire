package com.example.langfire_app.domain.model

data class WordProgress(
    val id: Int = 0,
    val knowledgeCoeff: Float? = null,
    val lastReviewed: Long? = null,
    val correctCount: Int? = null,
    val incorrectCount: Int? = null,
    val srsInterval: Int? = null,
    val srsEaseFactor: Float? = null,
    val srsRepetition: Int? = null,
    val nextReviewAt: Long? = null,
    val profileId: Int,
    val wordId: Int
)
