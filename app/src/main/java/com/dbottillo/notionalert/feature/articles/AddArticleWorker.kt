package com.dbottillo.notionalert.feature.articles

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.notionalert.data.AppConstant
import com.dbottillo.notionalert.feature.home.HomeRepository
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AddArticleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HomeRepository
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val title =
                inputData.getString(ADD_ARTICLE_DATA_TITLE) ?: return@withContext Result.failure()
            val url =
                inputData.getString(ADD_ARTICLE_DATA_URL) ?: return@withContext Result.failure()
            val response = repository.addPage(AppConstant.ARTICLES_DATABASE_ID, title, url)
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
