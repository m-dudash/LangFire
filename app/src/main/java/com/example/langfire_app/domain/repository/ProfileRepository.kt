package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfileById(id: Int): Profile?
    suspend fun getActiveProfile(): Profile?
    suspend fun saveProfile(profile: Profile): Long
    suspend fun updateProfile(profile: Profile)
    suspend fun addXp(profileId: Int, amount: Int)
    suspend fun updateStreak(profileId: Int, streakDays: Int, lastActiveDate: Long)
    suspend fun setXpMultiplier(profileId: Int, multiplier: Int, expiresAt: Long?)
    suspend fun clearXpMultiplier(profileId: Int)
    suspend fun addFreeze(profileId: Int)
    suspend fun consumeFreeze(profileId: Int)
}
