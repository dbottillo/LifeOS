package com.dbottillo.lifeos.notification

interface NotificationProvider {
    fun updateNextActions(text: String)
    fun clear()
    fun createNotificationChannel()
}
