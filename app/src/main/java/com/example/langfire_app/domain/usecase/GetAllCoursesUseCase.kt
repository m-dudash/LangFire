package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.Course
import com.example.langfire_app.domain.repository.CourseRepository
import javax.inject.Inject

class GetAllCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(): List<Course> = courseRepository.getAllCourses()
}