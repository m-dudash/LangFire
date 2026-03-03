package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.LibraryLevelSection
import com.example.langfire_app.domain.repository.CourseRepository
import com.example.langfire_app.domain.repository.ProfileRepository
import javax.inject.Inject

class GetLibraryContentUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val courseRepository: CourseRepository
){
    suspend operator fun invoke(courseId: Int): List<LibraryLevelSection>{
        val profile = profileRepository.getActiveProfile() ?: throw IllegalStateException("No active profile")

        return courseRepository.getLibraryContent(courseId, profile.id)


    }
}