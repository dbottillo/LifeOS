package com.dbottillo.notionalert

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.dbottillo.notionalert.feature.home.NEXT_ACTIONS
import com.dbottillo.notionalert.feature.home.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WidgetProvider: AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
           /* val pendingIntent: PendingIntent = PendingIntent.getActivity(
                *//* context = *//* context,
                *//* requestCode = *//*  0,
                *//* intent = *//* Intent(context, ExampleActivity::class.java),
                *//* flags = *//* PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )*/

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget
            ).apply {
                this.setTextViewText(R.id.widget_next_actions, "Loading...")

                val exampleData = runBlocking { context.dataStore.data.first() }
                this.setTextViewText(R.id.widget_next_actions, exampleData[NEXT_ACTIONS]?.replace("\n", "\n\n") ?: "No actions")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}