package com.dbottillo.notionalert

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.dbottillo.notionalert.feature.home.PocketStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PocketWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var pocketStorage: PocketStorage

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
        val views = RemoteViews(context.packageName, R.layout.pocket_widget)
        val total = runBlocking { pocketStorage.numberToReadFlow.first().toString() }
        views.setTextViewText(R.id.pocket_widget_count, total)
        context.packageManager.getLaunchIntentForPackage("com.ideashower.readitlater.pro")?.let {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */
                context,
                /* requestCode = */
                0,
                /* intent = */
                it,
                /* flags = */
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.pocket_widget_count, pendingIntent)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
