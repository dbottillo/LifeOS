package com.dbottillo.lifeos.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotionEntry(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "emoji") val emoji: String?,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "color") val color: String?,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "start_date") val startDate: String?,
    @ColumnInfo(name = "end_date") val endDate: String?,
    @ColumnInfo(name = "time_zone") val timeZone: String?
)
