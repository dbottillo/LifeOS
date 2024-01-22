package com.dbottillo.lifeos.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Article::class, Log::class, NotionEntry::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    abstract fun logDao(): LogDao

    abstract fun notionEntryDao(): NotionEntryDao
}
