package com.dbottillo.lifeos.feature.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.RemoteViews
import androidx.compose.material3.ExperimentalMaterial3Api
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.feature.home.HomeActivity
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

    @OptIn(ExperimentalMaterial3Api::class)
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.articles_widget)
        val total = runBlocking { db.articleDao().getAll().first() }
        val inbox = total.count { !it.longRead }.toString()
        val longRead = total.count { it.longRead }.toString()
        val spannable = SpannableString("$inbox\n$longRead")
        spannable.setSpan(
            RelativeSizeSpan(2f),
            0, inbox.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        views.setTextViewText(R.id.articles_widget_count, spannable)
        val intent = Intent(context, HomeActivity::class.java)
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
