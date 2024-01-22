package com.dbottillo.lifeos.feature.widgets

import android.content.Intent
import android.widget.RemoteViewsService
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NextActionsWidgetService : RemoteViewsService() {

    @Inject lateinit var tasksRepository: TasksRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NextActionsRemoteViewsFactory(this, intent, tasksRepository)
    }
}
