package com.dbottillo.notionalert

interface NotificationProvider {
    fun update(data: NotionPage)
    fun clear()
}
