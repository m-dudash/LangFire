package com.example.langfire_app.domain.model

data class UserStatsBackup(
    val profile: Profile,
    val wordProgress: List<WordProgress>,
    val achievements: List<Achievement>,
    val behaviors: List<Behavior>,
    val lastCourseId: Int? = null
)
