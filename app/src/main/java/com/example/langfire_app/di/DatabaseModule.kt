package com.example.langfire_app.di

import android.content.Context
import androidx.room.Room
import com.example.langfire_app.data.local.AppDatabase
import com.example.langfire_app.data.local.DatabaseCallback
import com.example.langfire_app.data.local.dao.*
import com.example.langfire_app.data.repository.*
import com.example.langfire_app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "langfire_db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideBehaviorDao(db: AppDatabase): BehaviorDao = db.behaviorDao()

    @Provides
    fun provideRuleDao(db: AppDatabase): RuleDao = db.ruleDao()

    @Provides
    fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()

    @Provides
    fun provideWordProgressDao(db: AppDatabase): WordProgressDao = db.wordProgressDao()

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao {
        return db.courseDao()
    }

    @Provides
    fun provideAppSettingsDao(db: AppDatabase): AppSettingsDao {
        return db.appSettingsDao()
    }

}

/**
 * Hilt module for binding repository interfaces to their implementations.
 * Follows Clean Architecture: domain layer depends on interfaces,
 * data layer provides implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBehaviorRepository(
        impl: BehaviorRepositoryImpl
    ): BehaviorRepository

    @Binds
    @Singleton
    abstract fun bindRuleRepository(
        impl: RuleRepositoryImpl
    ): RuleRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        impl: AchievementRepositoryImpl
    ): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}