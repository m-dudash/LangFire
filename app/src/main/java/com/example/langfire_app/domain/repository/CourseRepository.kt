package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.model.LibraryLevelSection

interface CourseRepository {
    suspend fun getAllCourses(): List<Course>
    suspend fun getCourseById(id: Int): Course?
    suspend fun saveCourse(course: Course)
    suspend fun getLibraryContent(courseId: Int, profileId: Int): List<LibraryLevelSection>
}