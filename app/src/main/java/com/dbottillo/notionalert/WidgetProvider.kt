package com.dbottillo.notionalert

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.NonNull
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach {
            updateAppWidget(context, appWidgetManager, it)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget)
        setRemoteAdapter(context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setRemoteAdapter(
        context: Context,
        @NonNull views: RemoteViews
    ) {
        views.setRemoteAdapter(
            R.id.widget_next_actions,
            Intent(context, WidgetService::class.java)
        )
    }
}
