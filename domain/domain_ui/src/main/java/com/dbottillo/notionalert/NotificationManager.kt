package com.dbottillo.notionalert

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dbottillo.notionalert.domain.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationManager @Inject constructor(
    @ApplicationContext val context: Context
) : NotificationProvider {

    override fun updateMainPage(text: String) {
        with(NotificationManagerCompat.from(context)) {
            notify(
                MAIN_NOTIFICATION_ID,
                getNotificationBuilder(
                    "Main Page",
                    text
                ).build()
            )
        }
    }

    override fun updateNextActions(text: String) {
        with(NotificationManagerCompat.from(context)) {
            notify(
                MAIN_DATABASE_ID,
                getNotificationBuilder(
                    "Next actions",
                    text
                ).build()
            )
        }
    }

    override fun clear() {
        with(NotificationManagerCompat.from(context)) {
            cancel(MAIN_NOTIFICATION_ID)
            cancel(MAIN_DATABASE_ID)
        }
    }

    private fun getNotificationBuilder(title: String, text: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}
