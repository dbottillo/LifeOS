package com.dbottillo.lifeos.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Log(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "level") val level: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val createdAt: Long
)
