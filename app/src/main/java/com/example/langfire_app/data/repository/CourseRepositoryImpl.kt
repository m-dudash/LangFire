package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.CourseDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.repository.CourseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao
) : CourseRepository {
    override suspend fun getAllCourses(): List<Course> = courseDao.getAll().map { it.toDomain() }
    override suspend fun getCourseById(id: Int): Course? = courseDao.getById(id)?.toDomain()
    override suspend fun saveCourse(course: Course) { courseDao.insert(course.toEntity()) }
}