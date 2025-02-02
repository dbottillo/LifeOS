package com.dbottillo.lifeos.network

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RefreshManager @Inject constructor(
    @ApplicationContext val context: Context
) : RefreshProvider {

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun start() {
        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval = REFRESH_WORKER_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = REFRESH_WORKER_FLEX_INTERVAL,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            REFRESH_WORKER_PERIODIC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
        val dailyRequest =
            PeriodicWorkRequestBuilder<DailyRefreshWorker>(
                repeatInterval = REFRESH_WORKER_DAILY_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
                flexTimeInterval = REFRESH_WORKER_DAILY_FLEX_INTERVAL,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10L, TimeUnit.MINUTES)
                .build()
        workManager.enqueueUniquePeriodicWork(
            REFRESH_WORKER_DAILY_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }

    override fun stop() {
        workManager.cancelUniqueWork(REFRESH_WORKER_PERIODIC_TAG)
        workManager.cancelUniqueWork(REFRESH_WORKER_DAILY_TAG)
    }

    override fun immediate() {
        val immediateRequest = OneTimeWorkRequestBuilder<RefreshWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(immediateRequest)
        val immediateDailyRequest = OneTimeWorkRequestBuilder<DailyRefreshWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(immediateRequest)
        workManager.enqueue(immediateDailyRequest)
    }

    override fun periodicStatus(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosFlow(WorkQuery.fromUniqueWorkNames(REFRESH_WORKER_PERIODIC_TAG))
    }

    override fun dailyStatus(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosFlow(WorkQuery.fromUniqueWorkNames(REFRESH_WORKER_DAILY_TAG))
    }
}

private const val REFRESH_WORKER_INTERVAL = 30L
private const val REFRESH_WORKER_FLEX_INTERVAL = 15L
private const val REFRESH_WORKER_PERIODIC_TAG = "periodic_refresh"
private const val REFRESH_WORKER_DAILY_INTERVAL = 1L
private const val REFRESH_WORKER_DAILY_FLEX_INTERVAL = 15L
private const val REFRESH_WORKER_DAILY_TAG = "daily_refresh"
