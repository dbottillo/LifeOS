package com.dbottillo.notionalert

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
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
        val intent = Intent(context, WidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val remoteView = RemoteViews(context.packageName, R.layout.widget).apply {
            setRemoteAdapter(R.id.widget_next_actions, intent)
            setEmptyView(R.id.widget_next_actions, R.id.empty_view)
        }

        val linkIntent = Intent(context, WidgetProvider::class.java)
        linkIntent.action = LINK_ACTION
        linkIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        val linkPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            linkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        remoteView.setPendingIntentTemplate(R.id.widget_next_actions, linkPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, remoteView)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LINK_ACTION) {
            val link: String? = intent.getStringExtra(LINK_URL)
            link?.let {
                if (it == "refresh") {
                    val immediateRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
                    WorkManager.getInstance(context).enqueue(immediateRequest)
                } else {
                    val intentUrl = Intent(Intent.ACTION_VIEW)
                    intentUrl.data = Uri.parse(it)
                    intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentUrl)
                }
            }
        }
        super.onReceive(context, intent)
    }
}

const val LINK_ACTION = "com.dbottillo.notionalert.list.LINK_ACTION"
const val LINK_URL = "com.dbottillo.notionalert.list.LINK_URL"
const val EXTRA_ITEM = "com.dbottillo.notionalert.list.EXTRA_ITEM"
