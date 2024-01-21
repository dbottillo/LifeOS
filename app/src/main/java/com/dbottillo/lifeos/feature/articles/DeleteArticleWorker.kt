package com.dbottillo.lifeos.feature.articles

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.ArchiveBodyRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@HiltWorker
class DeleteArticleWorker @AssistedInject constructor(
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
            val entry = articleRepository.findArticle(uuid)
            logsRepository.addEntry(
                tag = LogTags.DELETE_ARTICLE_WORKER,
                level = LogLevel.INFO,
                message = "Delete [${entry.title}]"
            )
            val response = api.archivePage(uuid, ArchiveBodyRequest(true))
            if (response.isSuccessful) {
                logsRepository.addEntry(
                    tag = LogTags.DELETE_ARTICLE_WORKER,
                    level = LogLevel.INFO,
                    message = "Delete [${entry.title}]: successful"
                )
                return@withContext Result.success()
            }
            logsRepository.addEntry(
                tag = LogTags.DELETE_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Error deleting [${entry.title}]: ${JSONObject(response.errorBody()?.string() ?: "")}"
            )
            return@withContext if (this@DeleteArticleWorker.runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            logsRepository.addEntry(
                tag = LogTags.DELETE_ARTICLE_WORKER,
                level = LogLevel.ERROR,
                message = "Error $error}"
            )
            return@withContext if (this@DeleteArticleWorker.runAttemptCount > MAX_RUN_ATTEMPTS) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}
