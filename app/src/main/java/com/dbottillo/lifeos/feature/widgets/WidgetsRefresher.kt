package com.dbottillo.lifeos.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetsRefresher @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun refreshAll() {
        val articlesIntent = Intent(context.applicationContext, ArticlesWidgetProvider::class.java)
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

        val actionsIntent = Intent(context.applicationContext, NextActionsWidgetProvider::class.java)
        actionsIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val actionsIntentIds: IntArray = widgetManager.getAppWidgetIds(
            ComponentName(
                context,
                NextActionsWidgetProvider::class.java
            )
        )
        actionsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, actionsIntentIds)
        context.sendBroadcast(actionsIntent)
    }
}
