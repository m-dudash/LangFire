package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import com.example.langfire_app.domain.repository.UnitDetailsRepository
import javax.inject.Inject

sealed class MarkWordResult {
    object Success : MarkWordResult()
    data class QueueFull(val message: String) : MarkWordResult()
    object NoProfile : MarkWordResult()
    object NoCourse : MarkWordResult()
}

class MarkUnitWordUseCase @Inject constructor(
    private val repository: UnitDetailsRepository,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(wordId: Int, coeff: Float): MarkWordResult {
        val profile = profileRepository.getActiveProfile() ?: return MarkWordResult.NoProfile

        if (coeff == 0.0f) {
            val courseId = settingsRepository.getCurrentCourseId()
                ?: return MarkWordResult.NoCourse
            val toLearnCount = repository.countToLearnByCourse(
                profileId = profile.id,
                courseId = courseId,
                nowMs = System.currentTimeMillis()
            )
            if (toLearnCount >= 50) {
                return MarkWordResult.QueueFull(
                    "You already have 50 words in your To Learn queue. Complete a session before adding more!"
                )
            }
        }

        repository.saveWordMark(profile.id, wordId, coeff)
        return MarkWordResult.Success
    }
}
