package com.example.langfire_app.data.local.entities

data class UnitWithStats(
    val unitId: Int,
    val unitName: String,
    val levelName: String,
    val levelId: Int,
    val totalWords: Int,
    val learnedWords: Int
)