package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotionEntryDao {

    @Query("SELECT * FROM notionEntry WHERE type = 'alert'")
    fun getNextActions(): Flow<List<NotionEntry>>

    @Query("SELECT * FROM notionEntry WHERE type = 'Project'")
    fun getProjects(): Flow<List<NotionEntry>>

    @Query("SELECT * FROM notionEntry WHERE type = 'Area'")
    fun getAreas(): Flow<List<NotionEntry>>

    @Query("SELECT * FROM notionEntry WHERE type = 'Idea'")
    fun getIdeas(): Flow<List<NotionEntry>>

    @Query("SELECT * FROM notionEntry WHERE type = 'Resource'")
    fun getResources(): Flow<List<NotionEntry>>

    @Insert
    suspend fun insertAll(vararg entries: NotionEntry)

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndInsertAll(entries: List<NotionEntry>) {
        deleteNextActions()
        insertAll(*entries.toTypedArray())
    }

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndSaveAllProjectsAreaResourcesAndIdeas(projects: List<NotionEntry>) {
        deleteNonAlerts()
        insertAll(*projects.toTypedArray())
    }

    @Query("DELETE FROM notionEntry WHERE type = 'alert'")
    suspend fun deleteNextActions()

    @Query("DELETE FROM notionEntry WHERE type != 'alert'")
    suspend fun deleteNonAlerts()
}
