package com.dbottillo.lifeos.feature.articles

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.NotionStatus
import com.dbottillo.lifeos.network.NotionUpdateProperty
import com.dbottillo.lifeos.network.UpdateBodyRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@HiltWorker
class UpdateStatusArticleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: ApiInterface,
    private val articleRepository: ArticleRepository,
    private val logsRepository: LogsRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val uuid =
                inputData.getString(ARTICLE_DATA_UUID) ?: return@withContext Result.failure()
            val status =
                inputData.getString(ARTICLE_DATA_STATUS) ?: return@withContext Result.failure()
            val entry = articleRepository.findArticle(uuid)
            logsRepository.addEntry(
                tag = LogTags.UPDATE_ARTICLE_WORKER,
                level = LogLevel.INFO,
                message = "Update [${entry.url}] with status: $status"
            )
            val response = api.updatePage(
                uuid,
                UpdateBodyRequest(
                    properties = mapOf(
                        "Status" to NotionUpdateProperty(status = NotionStatus(status))
                    )
                )
            )
            if (response.isSuccessful) {
                logsRepository.addEntry(
                    tag = LogTags.UPDATE_ARTICLE_WORKER,
                    level = LogLevel.INFO,
                    message = "Update [${entry.url}]: successful"
                )
                return@withContext Result.success()
            }
            val error = JSONObject(response.errorBody()?.string() ?: "")
            logsRepository.addEntry(
                tag = LogTags.UPDATE_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Error updating [${entry.url}]: $error"
            )
            return@withContext if (this@UpdateStatusArticleWorker.runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            logsRepository.addEntry(
                tag = LogTags.UPDATE_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Error updating $error"
            )
            return@withContext if (this@UpdateStatusArticleWorker.runAttemptCount > MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}
