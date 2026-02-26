package com.example.langfire_app.di

import com.example.langfire_app.data.local.dao.AppSettingsDao
import com.example.langfire_app.data.local.dao.CourseDao
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
    fun provideCourseRepository(
        courseDao: CourseDao
    ): CourseRepository {
        return CourseRepositoryImpl(courseDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: AppSettingsDao
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDao)
    }
}