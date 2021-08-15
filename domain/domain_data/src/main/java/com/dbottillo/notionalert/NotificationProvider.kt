package com.dbottillo.notionalert

interface NotificationProvider {
    fun updateMainPage(text: String)
    fun updateNextActions(text: String)
    fun clear()
    fun createNotificationChannel()
}
