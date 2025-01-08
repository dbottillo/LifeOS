package com.dbottillo.lifeos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.notification.NotificationProvider
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
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

    @Inject
    lateinit var networkFlipperPlugin: NetworkFlipperPlugin

    override fun onCreate() {
        super.onCreate()

        notificationProvider.createNotificationChannel()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            repository.init()
        }

        SoLoader.init(this, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.addPlugin(networkFlipperPlugin)
            client.start()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.VERBOSE)
            .build()
}
