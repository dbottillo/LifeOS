package com.dbottillo.notionalert

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RefreshManager @Inject constructor(
    @ApplicationContext val context: Context
) : RefreshProvider {

    override fun start() {
        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval = REFRESH_WORKER_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = REFRESH_WORKER_FLEX_INTERVAL,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .build()
        WorkManager.getInstance(context).enqueue(refreshRequest)
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelAllWork()
    }

    override fun immediate() {
        val immediateRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
}

private const val REFRESH_WORKER_INTERVAL = 30L
private const val REFRESH_WORKER_FLEX_INTERVAL = 15L
