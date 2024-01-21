package com.dbottillo.lifeos.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Article::class, Log::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    abstract fun logDao(): LogDao
}
