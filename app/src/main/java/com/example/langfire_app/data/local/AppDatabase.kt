package com.example.langfire_app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
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
    version = 13,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12, spec = AppDatabase.DeleteSuperWinColumn::class),
        AutoMigration(from = 12, to = 13)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    @DeleteColumn(tableName = "profile", columnName = "has_super_win")
    class DeleteSuperWinColumn : AutoMigrationSpec

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