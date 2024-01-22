package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.nio.file.Files.delete

@Dao
interface NotionEntryDao {

    @Query("SELECT * FROM notionEntry WHERE type = 'alert'")
    fun getNextActions(): Flow<List<NotionEntry>>

    @Insert
    suspend fun insertAll(vararg entries: NotionEntry)

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndInsertAll(entries: List<NotionEntry>) {
        delete()
        insertAll(*entries.toTypedArray())
    }

    @Query("DELETE FROM notionEntry")
    suspend fun delete()
}
