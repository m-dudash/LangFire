package com.example.langfire_app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.langfire_app.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val stats = syncRepository.getLocalStats()
            val result = syncRepository.uploadStats(stats)
            if (result.isSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            // Retry if it's a network error, otherwise fail permanently
            Result.retry()
        }
    }
}
