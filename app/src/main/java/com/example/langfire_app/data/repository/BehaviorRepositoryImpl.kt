package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.BehaviorDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.repository.BehaviorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorRepositoryImpl @Inject constructor(
    private val behaviorDao: BehaviorDao
) : BehaviorRepository {

    override suspend fun saveBehavior(behavior: Behavior): Long {
        return behaviorDao.insert(behavior.toEntity())
    }

    override suspend fun getBehaviorsByProfile(profileId: Int): List<Behavior> {
        return behaviorDao.getAllByProfileId(profileId).map { it.toDomain() }
    }

    override suspend fun getBehaviorsByType(profileId: Int, type: String): List<Behavior> {
        return behaviorDao.getByType(profileId, type).map { it.toDomain() }
    }

    override suspend fun getBehaviorsByTypeAfter(
        profileId: Int,
        type: String,
        fromTimestamp: Long
    ): List<Behavior> {
        return behaviorDao.getByTypeAfter(profileId, type, fromTimestamp).map { it.toDomain() }
    }

    override suspend fun getBehaviorsByTypeInRange(
        profileId: Int,
        type: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<Behavior> {
        return behaviorDao.getByTypeInRange(profileId, type, fromTimestamp, toTimestamp)
            .map { it.toDomain() }
    }
}
