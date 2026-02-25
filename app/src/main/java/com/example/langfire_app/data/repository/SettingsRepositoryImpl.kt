package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.AppSettingsDao
import com.example.langfire_app.data.local.entities.AppSettingEntity
import com.example.langfire_app.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: AppSettingsDao
) : SettingsRepository {
    override suspend fun getCurrentCourseId(): Int? =
        settingsDao.getSetting("current_course_id")?.toIntOrNull()

    override suspend fun setCurrentCourseId(courseId: Int) {
        settingsDao.insert(AppSettingEntity("current_course_id", courseId.toString()))
    }
}