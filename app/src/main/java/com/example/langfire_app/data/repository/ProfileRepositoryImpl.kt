package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.ProfileDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.Profile
import com.example.langfire_app.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun getProfileById(id: Int): Profile? {
        return profileDao.getById(id)?.toDomain()
    }

    override suspend fun getActiveProfile(): Profile? {
        return profileDao.getActiveProfile()?.toDomain()
    }

    override suspend fun saveProfile(profile: Profile): Long {
        return profileDao.insert(profile.toEntity())
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.update(profile.toEntity())
    }

    override suspend fun addXp(profileId: Int, amount: Int) {
        profileDao.addXp(profileId, amount)
    }

    override suspend fun updateStreak(profileId: Int, streakDays: Int, lastActiveDate: Long) {
        profileDao.updateStreak(profileId, streakDays, lastActiveDate)
    }
}
