package com.example.langfire_app.di

import com.example.langfire_app.data.local.AppDatabase
import com.example.langfire_app.data.local.dao.AppSettingsDao
import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.dao.UnitDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.repository.CourseRepositoryImpl
import com.example.langfire_app.data.repository.SettingsRepositoryImpl
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideUnitDao(db: AppDatabase): UnitDao = db.unitDao()

    @Provides
    @Singleton
    fun provideCourseRepository(
        courseDao: CourseDao,
        unitDao: UnitDao,
        wordProgressDao: WordProgressDao
    ): CourseRepository {
        return CourseRepositoryImpl(courseDao, unitDao, wordProgressDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: AppSettingsDao
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDao)
    }
}