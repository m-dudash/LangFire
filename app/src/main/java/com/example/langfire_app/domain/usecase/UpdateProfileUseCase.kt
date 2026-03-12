package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile) {
        profileRepository.updateProfile(profile)
    }
}
