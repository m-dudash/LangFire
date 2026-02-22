package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Profile

/**
 * Repository interface for user profile.
 * Defined in the domain layer — implementation is in the data layer.
 */
interface ProfileRepository {
    suspend fun getProfileById(id: Int): Profile?
    suspend fun getActiveProfile(): Profile?
    suspend fun saveProfile(profile: Profile): Long
    suspend fun updateProfile(profile: Profile)
    suspend fun addXp(profileId: Int, amount: Int)
    suspend fun updateStreak(profileId: Int, streakDays: Int, lastActiveDate: Long)
}
