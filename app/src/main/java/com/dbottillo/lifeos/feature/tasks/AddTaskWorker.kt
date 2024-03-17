package com.dbottillo.lifeos.feature.tasks

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.MAX_RUN_ATTEMPTS
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.network.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AddTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val logsRepository: LogsRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val title = inputData.getString(ADD_PAGE_TITLE) ?: ""
            val url =
                inputData.getString(ADD_PAGE_URL) ?: return@withContext Result.failure()
            logsRepository.addEntry(
                tag = LogTags.ADD_TASK_WORKER,
                level = LogLevel.INFO,
                message = "Adding [$title] for url: $url"
            )
            val result = tasksRepository.addTask(AppConstant.GTD_ONE_DATABASE_ID, title, url)
            if (result is ApiResult.Success) {
                logsRepository.addEntry(
                    tag = LogTags.ADD_TASK_WORKER,
                    level = LogLevel.INFO,
                    message = "Article [$url] added successfully"
                )
                return@withContext Result.success()
            }
            logsRepository.addEntry(
                tag = LogTags.ADD_TASK_WORKER,
                level = LogLevel.ERROR,
                message = "Failed with ${(result as ApiResult.Error).exception}"
            )
            return@withContext if (this@AddTaskWorker.runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            logsRepository.addEntry(
                tag = LogTags.ADD_TASK_WORKER,
                level = LogLevel.ERROR,
                message = "Failed with $error"
            )
            return@withContext if (this@AddTaskWorker.runAttemptCount < MAX_RUN_ATTEMPTS) {
                Result.retry()
            } else {
                Result.success()
            }
        }
    }
}
