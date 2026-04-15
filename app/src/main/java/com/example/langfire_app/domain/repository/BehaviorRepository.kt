package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Behavior

interface BehaviorRepository {
    suspend fun saveBehavior(behavior: Behavior): Long
    suspend fun getBehaviorsByProfile(profileId: Int): List<Behavior>
    suspend fun getBehaviorsByType(profileId: Int, type: String): List<Behavior>
    suspend fun getBehaviorsByTypeAfter(profileId: Int, type: String, fromTimestamp: Long): List<Behavior>
    suspend fun getBehaviorsByTypeInRange(
        profileId: Int,
        type: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<Behavior>
}
