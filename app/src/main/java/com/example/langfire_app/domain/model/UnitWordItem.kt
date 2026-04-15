package com.example.langfire_app.domain.model

data class UnitWordItem(
    val wordId: Int,
    val word: String,
    val translation: String,
    val knowledgeCoeff: Float?,
    val audioPath: String?
)
