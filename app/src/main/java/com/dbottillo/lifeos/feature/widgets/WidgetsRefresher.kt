package com.dbottillo.lifeos.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetsRefresher @Inject constructor(
    @ApplicationContext val context: Context
) {

    suspend fun refreshAll() {
        val articlesIntent = Intent(context, ArticlesWidgetProvider::class.java)
        articlesIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val widgetManager = AppWidgetManager.getInstance(context)
        val articlesIds: IntArray = widgetManager.getAppWidgetIds(
            ComponentName(
                context,
                ArticlesWidgetProvider::class.java
            )
        )
        articlesIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, articlesIds)
        context.sendBroadcast(articlesIntent)

        val manager = GlanceAppWidgetManager(context)
        val widget = OverviewWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
