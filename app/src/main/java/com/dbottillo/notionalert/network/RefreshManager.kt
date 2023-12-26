package com.dbottillo.notionalert.network

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RefreshManager @Inject constructor(
    @ApplicationContext val context: Context
) : RefreshProvider {

    private val workManager = WorkManager.getInstance(context)

    override fun start() {
        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval = REFRESH_WORKER_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = REFRESH_WORKER_FLEX_INTERVAL,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .build()
        workManager.enqueueUniquePeriodicWork(
            REFRESH_WORKER_PERIODIC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
    }

    override fun stop() {
        workManager.cancelUniqueWork(REFRESH_WORKER_PERIODIC_TAG)
    }

    override fun immediate() {
        val immediateRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
        workManager.enqueue(immediateRequest)
    }

    override fun workManagerStatus(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(REFRESH_WORKER_PERIODIC_TAG)
    }
}

private const val REFRESH_WORKER_INTERVAL = 30L
private const val REFRESH_WORKER_FLEX_INTERVAL = 15L
private const val REFRESH_WORKER_PERIODIC_TAG = "periodic_refresh"
