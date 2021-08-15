package com.dbottillo.notionalert

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dbottillo.notionalert.feature.home.HomeRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NotionAlertApp : Application(), Configuration.Provider {

    @Inject
    lateinit var repository: HomeRepository

    @Inject
    lateinit var notificationProvider: NotificationProvider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        notificationProvider.createNotificationChannel()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            repository.init()
        }

        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval = REFRESH_WORKER_INTERVAL,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = REFRESH_WORKER_FLEX_INTERVAL,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .build()
        WorkManager.getInstance(this).enqueue(refreshRequest)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

private const val REFRESH_WORKER_INTERVAL = 30L
private const val REFRESH_WORKER_FLEX_INTERVAL = 15L
