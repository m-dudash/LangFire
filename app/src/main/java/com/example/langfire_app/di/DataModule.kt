package com.example.langfire_app.di

import com.example.langfire_app.data.local.AppDatabase
import com.example.langfire_app.data.local.dao.AppSettingsDao
import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.dao.UnitDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.dao.BehaviorDao
import com.example.langfire_app.domain.repository.AuthRepository
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.repository.SyncRepository
import com.example.langfire_app.data.local.dao.AchievementDao
import com.example.langfire_app.data.local.dao.ProfileDao
import com.example.langfire_app.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
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

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository {
        return FirebaseAuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        authRepository: AuthRepository,
        db: FirebaseDatabase,
        profileDao: ProfileDao,
        wordProgressDao: WordProgressDao,
        achievementDao: AchievementDao,
        behaviorDao: BehaviorDao,
        appSettingsDao: AppSettingsDao,
        gson: Gson
    ): SyncRepository {
        return FirebaseSyncRepositoryImpl(authRepository, db, profileDao, wordProgressDao, achievementDao, behaviorDao, appSettingsDao, gson)
    }
}