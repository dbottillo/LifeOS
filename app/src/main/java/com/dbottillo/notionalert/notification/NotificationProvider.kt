package com.dbottillo.notionalert.notification

interface NotificationProvider {
    fun updateNextActions(text: String)
    fun clear()
    fun createNotificationChannel()
}
