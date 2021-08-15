package com.dbottillo.notionalert

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
class NotionAlertApp : Application(), androidx.work.Configuration.Provider {

    @Inject
    lateinit var repository: HomeRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

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

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

private const val REFRESH_WORKER_INTERVAL = 30L
private const val REFRESH_WORKER_FLEX_INTERVAL = 15L
