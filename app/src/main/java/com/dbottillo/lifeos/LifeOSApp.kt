package com.dbottillo.lifeos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.notification.NotificationProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LifeOSApp : Application(), Configuration.Provider {

    @Inject
    lateinit var repository: TasksRepository

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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
