package com.dbottillo.lifeos.feature.articles

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@HiltWorker
class AddArticleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val logsRepository: LogsRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val title = inputData.getString(ADD_ARTICLE_DATA_TITLE) ?: ""
            val url =
                inputData.getString(ADD_ARTICLE_DATA_URL) ?: return@withContext Result.failure()
            logsRepository.addEntry(
                tag = LogTags.ADD_ARTICLE_WORKER,
                level = LogLevel.INFO,
                message = "Adding [$title] for url: $url"
            )
            val response = tasksRepository.addTask(AppConstant.ARTICLES_DATABASE_ID, title, url)
            if (response.isSuccessful) {
                logsRepository.addEntry(
                    tag = LogTags.ADD_ARTICLE_WORKER,
                    level = LogLevel.INFO,
                    message = "Article [$url] added successfully"
                )
                return@withContext Result.success()
            }
            logsRepository.addEntry(
                tag = LogTags.ADD_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Failed with ${JSONObject(response.errorBody()?.string() ?: "")}"
            )
            return@withContext if (this@AddArticleWorker.runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            logsRepository.addEntry(
                tag = LogTags.ADD_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Failed with $error"
            )
            return@withContext if (this@AddArticleWorker.runAttemptCount < MAX_RUN_ATTEMPTS) {
                Result.retry()
            } else {
                Result.success()
            }
        }
    }
}
