package com.dbottillo.notionalert

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dbottillo.notionalert.feature.home.HomeStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    // Based on: https://dev.to/inspire_coding/android-widgets-update-using-kotlin-flow-room-and-dagger-hilt-e0e

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    @Inject
    lateinit var homeStorage: HomeStorage

   /* override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
            *//* val pendingIntent: PendingIntent = PendingIntent.getActivity(
                 *//**//* context = *//**//* context,
                *//**//* requestCode = *//**//*  0,
                *//**//* intent = *//**//* Intent(context, ExampleActivity::class.java),
                *//**//* flags = *//**//* PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )*//*

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget
            ).apply {
                this.setTextViewText(R.id.widget_next_actions, "Loading...")

                //val exampleData = runBlocking { context.dataStore.data.first() }

            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }*/

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)

        coroutineScope.launch {
            homeStorage.data.collect {
                val text = it.nextActions.replace("\n", "\n\n")

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val man = AppWidgetManager.getInstance(context)
                val ids = man.getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))

                for (appWidgetId in ids) {
                    updateAppWidget(
                        context, appWidgetManager, appWidgetId, text
                    )
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        text: String
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget).apply {
            this.setTextViewText(R.id.widget_next_actions, text)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
