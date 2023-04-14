package com.dbottillo.notionalert

interface NotificationProvider {
    fun updateNextActions(text: String)
    fun clear()
    fun createNotificationChannel()
}
