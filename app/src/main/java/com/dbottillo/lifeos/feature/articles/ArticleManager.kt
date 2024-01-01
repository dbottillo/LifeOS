package com.dbottillo.lifeos.feature.articles

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dbottillo.lifeos.db.Article
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("MagicNumber")
class ArticleManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val workManager by lazy { WorkManager.getInstance(context) }

    fun addArticle(title: String?, url: String) {
        val request = OneTimeWorkRequestBuilder<AddArticleWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(ARTICLE_WORKER_TAG)
            .setInputData(
                workDataOf(
                ADD_ARTICLE_DATA_TITLE to title,
                ADD_ARTICLE_DATA_URL to url
            )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueue(request)
    }

    fun deleteArticle(article: Article) {
        val request = OneTimeWorkRequestBuilder<DeleteArticleWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(ARTICLE_WORKER_TAG)
            .setInputData(
                workDataOf(
                ARTICLE_DATA_UUID to article.uid
            )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueue(request)
    }

    fun updateArticleStatus(article: Article, status: String) {
        val request = OneTimeWorkRequestBuilder<UpdateStatusArticleWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(ARTICLE_WORKER_TAG)
            .setInputData(
                workDataOf(
                ARTICLE_DATA_UUID to article.uid,
                ARTICLE_DATA_STATUS to status
            )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueue(request)
    }

    fun status(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagFlow(ARTICLE_WORKER_TAG)
    }
}

private const val ARTICLE_WORKER_TAG = "article"
internal const val ADD_ARTICLE_DATA_TITLE = "title"
internal const val ARTICLE_DATA_UUID = "uuid"
internal const val ADD_ARTICLE_DATA_URL = "url"
internal const val ARTICLE_DATA_STATUS = "status"
