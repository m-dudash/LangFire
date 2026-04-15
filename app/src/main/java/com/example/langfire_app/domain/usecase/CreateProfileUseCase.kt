package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.repository.AppSeeder
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.SettingsRepository
import javax.inject.Inject

class CreateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val appSeeder: AppSeeder
) {
    suspend operator fun invoke(
        userName: String,
        initialCourseId: Int,
        dailyGoal: Int,
        avatarPath: String? = null
    ) {
        val newProfile = Profile(
            name = userName,
            avatarPath = avatarPath,
            dailyWordGoal = dailyGoal
        )

        val profileId = profileRepository.saveProfile(newProfile).toInt()
        settingsRepository.setCurrentCourseId(initialCourseId)
        appSeeder.seedForProfile(profileId)
    }
}