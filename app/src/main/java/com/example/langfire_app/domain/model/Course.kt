package com.example.langfire_app.domain.model

data class Course(
    val id: Int = 0,
    val name: String,
    val targetLang: String,
    val targetLanguageId: Int = 1,
    val icon: String
)

data class CourseLevelInfo(
    val courseName: String,
    val courseIcon: String,
    val targetLang: String,
    val achievedLevel: String?,
    val targetLevel: String?,
    val wordsLearnedInTarget: Int,
    val totalWordsInTarget: Int
)