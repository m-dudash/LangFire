package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.UserStatsBackup
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    suspend fun uploadStats(stats: UserStatsBackup): Result<Unit>
    suspend fun downloadStats(): Result<UserStatsBackup?>
    suspend fun getLocalStats(): UserStatsBackup
    suspend fun restoreStats(stats: UserStatsBackup)
}
