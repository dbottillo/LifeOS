package com.dbottillo.notionalert.feature.widgets

import android.content.Intent
import android.widget.RemoteViewsService
import com.dbottillo.notionalert.feature.home.HomeStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NextActionsWidgetService : RemoteViewsService() {

    @Inject lateinit var homeStorage: HomeStorage

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NextActionsRemoteViewsFactory(this, intent, homeStorage)
    }
}
