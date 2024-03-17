package com.dbottillo.lifeos.feature.tasks

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@Suppress("MagicNumber")
class TaskManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val workManager by lazy { WorkManager.getInstance(context) }

    fun addTask(title: String?, url: String): Operation {
        val request = OneTimeWorkRequestBuilder<AddTaskWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(TASK_WORKER_TAG)
            .setInputData(
                workDataOf(
                    ADD_PAGE_TITLE to title,
                    ADD_PAGE_URL to url,
                    ADD_PAGE_ID to Random.nextInt()
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            )
            .build()
        return workManager.enqueue(request)
    }
}

private const val TASK_WORKER_TAG = "task"
internal const val ADD_PAGE_TITLE = "title"
internal const val ADD_PAGE_URL = "url"
internal const val ADD_PAGE_ID = "uuid"
