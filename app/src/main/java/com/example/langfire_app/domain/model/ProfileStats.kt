package com.example.langfire_app.domain.model

data class ProfileStats(
    val wordsLearned: Int = 0,
    val totalCorrect: Int = 0,
    val totalErrors: Int = 0,
    val toughestWord: String? = null,
    val courseProgress: List<CourseLevelInfo> = emptyList()
)

