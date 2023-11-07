package com.dbottillo.notionalert

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        /*context.packageManager.getLaunchIntentForPackage("com.ideashower.readitlater.pro")?.let {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
         */
        /* context = */
        /*
                context,
         */
        /* requestCode = */
        /*
                0,
         */
        /* intent = */
        /*
                it,
         */
        /* flags = */
        /*
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.pocket_widget_count, pendingIntent)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)*/

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.notion.so/dbottillo/ef1963ca16574555874f5c3dc2523b61?v=e0eb4cd891f9456e89da25314d772523"
                )
            )
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.pocket_widget_count, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
