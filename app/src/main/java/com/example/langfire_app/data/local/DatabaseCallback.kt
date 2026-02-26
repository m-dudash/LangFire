package com.example.langfire_app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.langfire_app.di.ApplicationScope
import com.example.langfire_app.data.local.entities.AppSettingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import com.example.langfire_app.data.local.GamificationSeeder

import com.example.langfire_app.data.local.entities.ProfileEntity

class DatabaseCallback @Inject constructor(
    private val database: Provider<AppDatabase>,
    private val gamificationSeeder: Provider<GamificationSeeder>,
    @ApplicationScope private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        val appDatabase = database.get()
        val profileDao = appDatabase.profileDao()
        val courseDao = appDatabase.courseDao()

        // Seed Courses (Dutch from English, Slovak from Russian)
        val dutchCourse = com.example.langfire_app.data.local.entities.CourseEntity(
            name = "Dutch (from English)",
            targetLang = "nl",
            icon = "🇳🇱"
        )
        val slovakCourse = com.example.langfire_app.data.local.entities.CourseEntity(
            name = "Slovak (from Russian)",
            targetLang = "sk",
            icon = "🇸🇰"
        )
        courseDao.insert(dutchCourse)
        courseDao.insert(slovakCourse)

        
    }
}
