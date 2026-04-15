package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.dao.UnitDao
import com.example.langfire_app.data.local.dao.WordProgressDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.util.CourseProgressCalculator
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.LibraryLevelSection
import com.example.langfire_app.domain.model.LevelProgressStats
import com.example.langfire_app.domain.model.LibraryUnit
import com.example.langfire_app.domain.repository.CourseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val unitDao: UnitDao,
    private val wordProgressDao: WordProgressDao
) : CourseRepository {
    override suspend fun getAllCourses(): List<Course> = courseDao.getAll().map { it.toDomain() }
    override suspend fun getCourseById(id: Int): Course? = courseDao.getById(id)?.toDomain()
    override suspend fun saveCourse(course: Course) { courseDao.insert(course.toEntity()) }

    override suspend fun getLibraryContent(courseId: Int, profileId: Int): List<LibraryLevelSection> {
        val rawUnits = unitDao.getUnitsWithStats(
            courseId = courseId,
            profileId = profileId,
            learnedThreshold = 0.8f
        )

        val levelStats = wordProgressDao.getWordLevelProgressByCourse(
            profileId = profileId,
            courseId = courseId,
            threshold = 0.8f
        ).map { LevelProgressStats(it.levelName, it.totalWords, it.learnedWords) }
        val progressInfo = CourseProgressCalculator.computeCourseProgress(levelStats)

        val levelsRank = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val targetLevelIndex = levelsRank.indexOf(progressInfo.targetLevel)
        val achievedLevelIndex = levelsRank.indexOf(progressInfo.achievedLevel ?: "")

        val maxUnlockedIndex = when {
            targetLevelIndex != -1   -> targetLevelIndex
            progressInfo.targetLevel == null && achievedLevelIndex != -1 -> levelsRank.lastIndex
            else                     -> 0
        }

        return rawUnits.groupBy { it.levelName }
            .map { (levelName, units) ->
                val levelIndex = levelsRank.indexOf(levelName)


                val isLocked = if (levelIndex == -1) true else levelIndex > maxUnlockedIndex

                LibraryLevelSection(
                    levelName = levelName,
                    isLocked = isLocked,
                    units = units.map { unit ->
                        LibraryUnit(
                            id = unit.unitId,
                            name = unit.unitName,
                            totalWords = unit.totalWords,
                            learnedWords = unit.learnedWords,
                            isCompleted = unit.totalWords > 0 && unit.learnedWords >= unit.totalWords
                        )
                    }
                )
            }
            .sortedBy { levelsRank.indexOf(it.levelName) }
    }

}