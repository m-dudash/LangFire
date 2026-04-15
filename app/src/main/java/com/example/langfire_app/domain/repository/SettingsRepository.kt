package com.example.langfire_app.domain.repository

interface SettingsRepository {
    suspend fun getCurrentCourseId(): Int?
    suspend fun setCurrentCourseId(courseId: Int)
}