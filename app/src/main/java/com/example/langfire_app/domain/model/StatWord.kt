package com.example.langfire_app.domain.model

data class StatWord(
    val wordId: Int,
    val word: String,
    val translation: String,
    val unitName: String,
    val knowledgeCoeff: Float?,
    val srsRepetition: Int?,
    val nextReviewAt: Long?,
    val audioPath: String?
)
