package com.dbottillo.notionalert

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dbottillo.notionalert.feature.home.HomeRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
