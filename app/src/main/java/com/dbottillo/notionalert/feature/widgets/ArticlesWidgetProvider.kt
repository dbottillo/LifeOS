package com.dbottillo.notionalert.feature.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.dbottillo.notionalert.R
import com.dbottillo.notionalert.db.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class ArticlesWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var db: AppDatabase

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
        val views = RemoteViews(context.packageName, R.layout.articles_widget)
        val total = runBlocking { db.articleDao().getAll().first().count().toString() }
        views.setTextViewText(R.id.articles_widget_count, total)
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
        views.setOnClickPendingIntent(R.id.articles_widget_count, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
