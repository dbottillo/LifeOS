package com.dbottillo.notionalert.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.notionalert.feature.home.HomeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HomeRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            repository.makeNetworkRequest()
            repository.fetchArticles()
            return@withContext Result.success()
        } catch (error: Throwable) {
            return@withContext Result.failure()
        }
    }
}
