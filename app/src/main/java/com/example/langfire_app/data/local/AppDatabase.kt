package com.example.langfire_app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.langfire_app.data.local.dao.*
import com.example.langfire_app.data.local.entities.*

@Database(
    entities = [
        LanguageEntity::class,
        LevelEntity::class,
        UnitEntity::class,
        ArticleEntity::class,
        WordTypeEntity::class,
        WordsEntity::class,
        TranslationEntity::class,
        ProfileEntity::class,
        WordProgressEntity::class,
        BehaviorEntity::class,
        AchievementEntity::class,
        RuleEntity::class,
        CourseEntity::class,
        AppSettingEntity::class,
        GenderEntity::class,
    ],
    version = 9,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    // Registration of DAOs
    abstract fun profileDao(): ProfileDao
    abstract fun behaviorDao(): BehaviorDao
    abstract fun ruleDao(): RuleDao
    abstract fun achievementDao(): AchievementDao
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun courseDao(): CourseDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun unitDao(): UnitDao
    abstract fun wordsDao(): WordsDao
}