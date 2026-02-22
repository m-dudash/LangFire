package com.example.langfire_app.data.local

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
        RuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Registration of DAOs
    abstract fun profileDao(): ProfileDao
    abstract fun behaviorDao(): BehaviorDao
    abstract fun ruleDao(): RuleDao
    abstract fun achievementDao(): AchievementDao
    abstract fun wordProgressDao(): WordProgressDao
}