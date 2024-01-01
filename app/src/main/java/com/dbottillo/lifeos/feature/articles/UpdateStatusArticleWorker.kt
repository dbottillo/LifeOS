package com.dbottillo.lifeos.feature.articles

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.NotionStatus
import com.dbottillo.lifeos.network.NotionUpdateProperty
import com.dbottillo.lifeos.network.UpdateBodyRequest
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateStatusArticleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: ApiInterface
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val uuid =
                inputData.getString(ARTICLE_DATA_UUID) ?: return@withContext Result.failure()
            val status =
                inputData.getString(ARTICLE_DATA_STATUS) ?: return@withContext Result.failure()
            val response = api.updatePage(
                uuid,
                UpdateBodyRequest(
                    properties = mapOf(
                        "Status" to NotionUpdateProperty(status = NotionStatus(status))
                    )
                )
            )
            if (response.isSuccessful) {
                return@withContext Result.success()
            }
            Firebase.crashlytics.recordException(Throwable(response.errorBody().toString()))
            return@withContext Result.retry()
        } catch (error: Throwable) {
            Firebase.crashlytics.recordException(error)
            return@withContext Result.retry()
        }
    }
}
