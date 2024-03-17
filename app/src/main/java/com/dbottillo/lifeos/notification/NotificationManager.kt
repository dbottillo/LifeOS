package com.dbottillo.lifeos.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.feature.widgets.NextActionsWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationManager @Inject constructor(
    @ApplicationContext val context: Context
) : NotificationProvider {

    fun sendOrUpdateInfoNotification(id: Int, title: String, text: String) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(
                id,
                getInfoNotificationBuilder(
                    title,
                    text
                ).build()
            )
        }
    }

    override fun updateNextActions(text: String) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(
                MAIN_DATABASE_ID,
                getMainNotificationBuilder(
                    "Next actions",
                    text,
                    NotificationCompat.PRIORITY_DEFAULT,
                    CHANNEL_NEXT_ID
                ).build()
            )
        }
        updateWidgets()
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, NextActionsWidgetProvider::class.java)
        )
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_next_actions)
    }

    override fun clear() {
        with(NotificationManagerCompat.from(context)) {
            cancel(MAIN_DATABASE_ID)
        }
    }

    override fun createNotificationChannel() {
        createNextActionsChannel()
        createInfoChannel()
    }

    private fun createNextActionsChannel() {
        val name = context.getString(R.string.channel_next_name)
        val descriptionText = context.getString(R.string.channel_next_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_NEXT_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createInfoChannel() {
        val name = context.getString(R.string.channel_info_main)
        val descriptionText = context.getString(R.string.channel_info_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_INFO_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainNotificationBuilder(
        title: String,
        text: String,
        priority: Int,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text.replace("\n", " · "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setPriority(priority)
            .setContentIntent(PendingIntent.getActivity(context, 0, urlIntent, FLAG_IMMUTABLE))
    }

    private fun getInfoNotificationBuilder(
        title: String,
        text: String,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_INFO_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private val urlIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(NOTION_NEXT_ACTIONS_LINK)
    )
}

@Suppress("MaxLineLength")
private const val NOTION_NEXT_ACTIONS_LINK = "https://www.notion.so/dbottillo/1ecf1aad5b75430686cb91676942e5f1?v=344ed86634eb4719bd3af351f0fff870"
