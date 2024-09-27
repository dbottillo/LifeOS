package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotionEntryDao {

    // The return value includes a POJO with a @Relation.
    // It is usually desired to annotate this method with @Transaction to avoid possibility
    // of inconsistent results between the POJO and its relations.
    // See https://developer.android.com/reference/androidx/room/Transaction.html for details.

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'alert'")
    fun getNextActions(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE status = 'Ongoing'")
    fun getOngoing(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Project'")
    fun getProjects(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Area'")
    fun getAreas(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Idea'")
    fun getIdeas(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Resource'")
    fun getResources(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Goal'")
    fun getGoals(): Flow<List<NotionEntryWithParent>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg entries: NotionEntry)

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndInsertAll(entries: List<NotionEntry>) {
        deleteNextActions()
        insertAll(*entries.toTypedArray())
    }

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndSaveAllProjectsAreaResourcesAndIdeas(entries: List<NotionEntry>) {
        deleteNonAlerts()
        insertAll(*entries.toTypedArray())
    }

    @Query("DELETE FROM notionEntry WHERE type = 'alert'")
    suspend fun deleteNextActions()

    @Query("DELETE FROM notionEntry WHERE type != 'alert'")
    suspend fun deleteNonAlerts()
}
