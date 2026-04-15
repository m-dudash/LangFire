package com.example.langfire_app.domain.model

data class SessionWord(
    val wordId: Int,
    val word: String,
    val translation: String,
    val article: String?,
    val gender: String?,
    val plural: String?,
    val wordType: String?,
    val exampleSentence: String?,
    val knowledgeCoeff: Float?,
    val nextReviewAt: Long?,
    val srsInterval: Int?,
    val srsEaseFactor: Float?,
    val srsRepetition: Int?,
    val correctCount: Int?,
    val incorrectCount: Int?,
    val audioPath: String?
)
