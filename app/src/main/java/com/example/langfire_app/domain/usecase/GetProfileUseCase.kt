package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case: Get the active user profile.
 *
 * Used across the app to display user info, XP, streak, etc.
 */
class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Profile? {
        return profileRepository.getActiveProfile()
    }

    suspend fun getById(id: Int): Profile? {
        return profileRepository.getProfileById(id)
    }
}
