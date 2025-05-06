package com.dbottillo.lifeos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DailyRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val logsRepository: LogsRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            tasksRepository.loadStaticResources(
                listOf("Folder", "Area", "Goal", "Resource")
            )
            return@withContext Result.success()
        } catch (error: Throwable) {
            logsRepository.addEntry(
                tag = LogTags.DAILY_REFRESH_WORKER,
                level = LogLevel.ERROR,
                message = "Failed to refresh static resources -> $error"
            )
            if (runAttemptCount < 3) {
                return@withContext Result.retry()
            }
            return@withContext Result.failure()
        }
    }
}
