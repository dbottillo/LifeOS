package com.dbottillo.notionalert

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotionAlertApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
