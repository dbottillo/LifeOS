package com.dbottillo.lifeos.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Article::class, Log::class, NotionEntry::class, BlockParagraph::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    abstract fun logDao(): LogDao

    abstract fun notionEntryDao(): NotionEntryDao

    abstract fun blockParagraphDao(): BlockParagraphDao
}
