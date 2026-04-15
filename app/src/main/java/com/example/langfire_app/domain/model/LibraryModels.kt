package com.example.langfire_app.domain.model

data class LibraryLevelSection(
    val levelName: String,
    val isLocked: Boolean,
    val units: List<LibraryUnit>
)

data class LibraryUnit(
    val id: Int,
    val name: String,
    val totalWords: Int,
    val learnedWords: Int,
    val isCompleted: Boolean
)