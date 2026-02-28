package com.example.langfire_app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.langfire_app.data.local.dao.AppSettingsDao
import com.example.langfire_app.data.local.dao.ProfileDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.entities.AppSettingEntity
import com.example.langfire_app.data.local.entities.ProfileEntity
import com.example.langfire_app.data.local.entities.WordProgressEntity
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.BehaviorRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random
import java.util.Locale

@Singleton
class DebugSeeder @Inject constructor(
    private val appDatabase: AppDatabase,
    private val profileDao: ProfileDao,
    private val wordProgressDao: WordProgressDao,
    private val appSettingsDao: AppSettingsDao,
    private val achievementRepository: AchievementRepository,
    private val behaviorRepository: BehaviorRepository,
    private val gamificationSeeder: GamificationSeeder
) {

    suspend fun seedIfNeeded() {
        val existing = appSettingsDao.getSetting(DEBUG_SEED_KEY)
        if (existing == DEBUG_SEED_VERSION) return

        val db = appDatabase.openHelper.writableDatabase

        seedLanguages(db)
        seedLevels(db)
        seedArticles(db)
        seedWordTypes(db)

        val courses = seedCourses(db)
        val unitsByCourse = seedUnits(db, courses)
        val wordIds = seedWords(db, courses, unitsByCourse)

        val profileIds = seedProfiles()
        val activeProfileId = profileIds.first()

        appSettingsDao.insert(AppSettingEntity("current_course_id", courses.first().id.toString()))
        appSettingsDao.insert(AppSettingEntity("daily_goal_words", "20"))
        appSettingsDao.insert(AppSettingEntity("notifications_enabled", "true"))

        gamificationSeeder.seedIfNeeded(activeProfileId)
        unlockAchievements(activeProfileId)
        seedBehaviors(profileIds)
        seedWordProgress(profileIds, wordIds)

        appSettingsDao.insert(AppSettingEntity(DEBUG_SEED_KEY, DEBUG_SEED_VERSION))
    }

    private fun seedLanguages(db: SupportSQLiteDatabase) {
        insert(db, "INSERT OR REPLACE INTO language (id, language) VALUES (?, ?)", 1, "Dutch")
        insert(db, "INSERT OR REPLACE INTO language (id, language) VALUES (?, ?)", 2, "Slovak")
        insert(db, "INSERT OR REPLACE INTO language (id, language) VALUES (?, ?)", 3, "English")
        insert(db, "INSERT OR REPLACE INTO language (id, language) VALUES (?, ?)", 4, "Russian")
    }

    private fun seedLevels(db: SupportSQLiteDatabase) {
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        levels.forEachIndexed { index, level ->
            insert(db, "INSERT OR REPLACE INTO level (id, name) VALUES (?, ?)", index + 1, level)
        }
    }

    private fun seedArticles(db: SupportSQLiteDatabase) {
        insert(db, "INSERT OR REPLACE INTO article (id, name) VALUES (?, ?)", 1, "de")
        insert(db, "INSERT OR REPLACE INTO article (id, name) VALUES (?, ?)", 2, "het")
        insert(db, "INSERT OR REPLACE INTO article (id, name) VALUES (?, ?)", 3, "-")
    }

    private fun seedWordTypes(db: SupportSQLiteDatabase) {
        insert(db, "INSERT OR REPLACE INTO word_type (id, type) VALUES (?, ?)", 1, "noun")
        insert(db, "INSERT OR REPLACE INTO word_type (id, type) VALUES (?, ?)", 2, "verb")
        insert(db, "INSERT OR REPLACE INTO word_type (id, type) VALUES (?, ?)", 3, "adjective")
        insert(db, "INSERT OR REPLACE INTO word_type (id, type) VALUES (?, ?)", 4, "phrase")
    }

    private fun seedCourses(db: SupportSQLiteDatabase): List<CourseSeed> {
        val courses = listOf(
            CourseSeed(id = 1, name = "Dutch (from English)", targetLang = "nl", icon = "🇳🇱", languageId = 1, wordPrefix = "nl_word_"),
            CourseSeed(id = 2, name = "Slovak (from Russian)", targetLang = "sk", icon = "🇸🇰", languageId = 2, wordPrefix = "sk_word_")
        )

        courses.forEach { course ->
            insert(
                db,
                "INSERT OR REPLACE INTO course (id, name, target_lang, icon) VALUES (?, ?, ?, ?)",
                course.id,
                course.name,
                course.targetLang,
                course.icon
            )
        }

        return courses
    }

    private fun seedUnits(
        db: SupportSQLiteDatabase,
        courses: List<CourseSeed>
    ): Map<Int, List<Int>> {
        val unitsByCourse = mutableMapOf<Int, List<Int>>()
        var nextUnitId = 1

        courses.forEach { course ->
            val unitIds = mutableListOf<Int>()
            for (unitIndex in 1..3) {
                val unitId = nextUnitId++
                unitIds.add(unitId)
                insert(
                    db,
                    "INSERT OR REPLACE INTO unit (id, name, course_id) VALUES (?, ?, ?)",
                    unitId,
                    "Unit $unitIndex",
                    course.id
                )
            }
            unitsByCourse[course.id] = unitIds
        }

        return unitsByCourse
    }

    private fun seedWords(
        db: SupportSQLiteDatabase,
        courses: List<CourseSeed>,
        unitsByCourse: Map<Int, List<Int>>
    ): List<Int> {
        val wordIds = mutableListOf<Int>()
        var nextWordId = 1000

        courses.forEach { course ->
            val unitIds = unitsByCourse[course.id].orEmpty()
            repeat(WORDS_PER_COURSE) { index ->
                val wordId = nextWordId++
                val unitId = unitIds[index / WORDS_PER_UNIT]
                val levelId = (index / WORDS_PER_LEVEL) + 1
                val articleId = (index % 3) + 1
                val wordTypeId = (index % 4) + 1
                val label = String.format(Locale.US, "%03d", index + 1)
                val word = "${course.wordPrefix}$label"
                val plural = if (wordTypeId == 1) "${word}s" else ""

                insert(
                    db,
                    """
                    INSERT OR REPLACE INTO words (
                        id, word, plural, audio_path, article_id, word_type_id, unit_id, level_id, language_id
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    wordId,
                    word,
                    plural,
                    "",
                    articleId,
                    wordTypeId,
                    unitId,
                    levelId,
                    course.languageId
                )

                wordIds.add(wordId)
            }
        }

        return wordIds
    }

    private suspend fun seedProfiles(): List<Int> {
        val now = System.currentTimeMillis()
        val profiles = listOf(
            ProfileEntity(name = "Student", xp = 1350, streakDays = 7, lastActiveDate = now - DAY_MS),
            ProfileEntity(name = "Explorer", xp = 420, streakDays = 2, lastActiveDate = now - 2 * DAY_MS),
            ProfileEntity(name = "Achiever", xp = 2200, streakDays = 14, lastActiveDate = now - DAY_MS / 2),
            ProfileEntity(name = "Tester", xp = 120, streakDays = 1, lastActiveDate = now - 3 * DAY_MS)
        )

        return profiles.map { profileDao.insert(it).toInt() }
    }

    private suspend fun unlockAchievements(profileId: Int) {
        val achievements = achievementRepository.getAchievementsByProfile(profileId)
        val unlockedIds = setOf(1, 3, 4, 10, 16)

        achievements
            .filter { it.id in unlockedIds }
            .forEach { achievement ->
                achievementRepository.updateAchievement(
                    achievement.copy(
                        unlocked = true,
                        title = when (achievement.id) {
                            1 -> "Perfectionist"
                            3 -> "First Steps"
                            4 -> "Beginner"
                            10 -> "First Flame"
                            16 -> "Lucky Spin"
                            else -> achievement.title
                        },
                        icon = when (achievement.id) {
                            1 -> "🎯"
                            3 -> "🚀"
                            4 -> "🌱"
                            10 -> "🔥"
                            16 -> "🎡"
                            else -> achievement.icon
                        }
                    )
                )
            }
    }

    private suspend fun seedBehaviors(profileIds: List<Int>) {
        val now = System.currentTimeMillis()
        val sessions = listOf(
            Behavior(
                type = "session_complete",
                timestamp = now - 2 * HOUR_MS,
                attributes = mapOf(
                    "correct_count" to "12",
                    "incorrect_count" to "2",
                    "accuracy" to "86",
                    "session_time" to "95"
                ),
                profileId = profileIds.first()
            ),
            Behavior(
                type = "session_complete",
                timestamp = now - 26 * HOUR_MS,
                attributes = mapOf(
                    "correct_count" to "8",
                    "incorrect_count" to "1",
                    "accuracy" to "89",
                    "session_time" to "140"
                ),
                profileId = profileIds.first()
            )
        )

        sessions.forEach { behaviorRepository.saveBehavior(it) }

        profileIds.drop(1).forEachIndexed { index, profileId ->
            behaviorRepository.saveBehavior(
                Behavior(
                    type = "app_open",
                    timestamp = now - (index + 1) * 6 * HOUR_MS,
                    attributes = mapOf("source" to "debug_seed"),
                    profileId = profileId
                )
            )
        }
    }

    private suspend fun seedWordProgress(profileIds: List<Int>, wordIds: List<Int>) {
        val now = System.currentTimeMillis()
        val random = Random(42)

        val plans = listOf(
            ProgressPlan(profileIds[0], learned = 70, practiced = 30, struggling = 10),
            ProgressPlan(profileIds[1], learned = 40, practiced = 25, struggling = 8),
            ProgressPlan(profileIds[2], learned = 90, practiced = 20, struggling = 5),
            ProgressPlan(profileIds[3], learned = 20, practiced = 10, struggling = 5)
        )

        plans.forEach { plan ->
            val shuffled = wordIds.shuffled(random)
            val learnedIds = shuffled.take(plan.learned)
            val practicedIds = shuffled.drop(plan.learned).take(plan.practiced)
            val strugglingIds = shuffled.drop(plan.learned + plan.practiced).take(plan.struggling)

            learnedIds.forEach { wordId ->
                insertProgress(plan.profileId, wordId, randomCoeff(random, 0.85f, 1.0f), now, random)
            }
            practicedIds.forEach { wordId ->
                insertProgress(plan.profileId, wordId, randomCoeff(random, 0.4f, 0.8f), now, random)
            }
            strugglingIds.forEach { wordId ->
                insertProgress(plan.profileId, wordId, randomCoeff(random, 0.1f, 0.4f), now, random)
            }
        }
    }

    private suspend fun insertProgress(
        profileId: Int,
        wordId: Int,
        knowledgeCoeff: Float,
        now: Long,
        random: Random
    ) {
        val correct = (knowledgeCoeff * 12).roundToInt().coerceAtLeast(1)
        val incorrect = random.nextInt(0, 4)
        val lastReviewed = now - random.nextInt(0, 30) * DAY_MS

        wordProgressDao.insert(
            WordProgressEntity(
                knowledgeCoeff = knowledgeCoeff,
                lastReviewed = lastReviewed,
                correctCount = correct,
                incorrectCount = incorrect,
                profileId = profileId,
                wordId = wordId
            )
        )
    }

    private fun randomCoeff(random: Random, min: Float, max: Float): Float {
        return min + random.nextFloat() * (max - min)
    }

    private fun insert(db: SupportSQLiteDatabase, sql: String, vararg args: Any) {
        db.execSQL(sql, args)
    }

    private data class CourseSeed(
        val id: Int,
        val name: String,
        val targetLang: String,
        val icon: String,
        val languageId: Int,
        val wordPrefix: String
    )

    private data class ProgressPlan(
        val profileId: Int,
        val learned: Int,
        val practiced: Int,
        val struggling: Int
    )

    private companion object {
        const val DEBUG_SEED_KEY = "debug_seed_v1"
        const val DEBUG_SEED_VERSION = "1"
        const val WORDS_PER_COURSE = 60
        const val WORDS_PER_UNIT = 20
        const val WORDS_PER_LEVEL = 10
        const val DAY_MS = 24 * 60 * 60 * 1000L
        const val HOUR_MS = 60 * 60 * 1000L
    }
}
