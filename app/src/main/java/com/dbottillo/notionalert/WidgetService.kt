package com.dbottillo.notionalert

import android.content.Intent
import android.widget.RemoteViewsService
import com.dbottillo.notionalert.feature.home.HomeStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetService : RemoteViewsService() {

    @Inject lateinit var homeStorage: HomeStorage

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NotionRemoteViewsFactory(this, intent, homeStorage)
    }
}
