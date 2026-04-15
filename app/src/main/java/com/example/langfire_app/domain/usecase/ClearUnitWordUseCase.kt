package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.UnitDetailsRepository
import javax.inject.Inject

class ClearUnitWordUseCase @Inject constructor(
    private val repository: UnitDetailsRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(wordId: Int) {
        val profile = profileRepository.getActiveProfile() ?: return
        repository.clearWordProgress(profile.id, wordId)
    }
}
