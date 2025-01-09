package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface NotionEntryDao {

    // The return value includes a POJO with a @Relation.
    // It is usually desired to annotate this method with @Transaction to avoid possibility
    // of inconsistent results between the POJO and its relations.
    // See https://developer.android.com/reference/androidx/room/Transaction.html for details.

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE start_date NOT NULL or status ='Inbox'")
    fun getInbox(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE status = 'Focus' and type !='Goal' and type !='Project' order by parentId")
    fun getFocus(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE status = 'Blocked' order by parentId")
    fun getBlocked(): Flow<List<NotionEntryWithParent>>

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
    suspend fun deleteAndSaveFocusInboxBlocked(entries: List<NotionEntry>) {
        getInbox().first().forEach { delete(it.notionEntry) }
        getFocus().first().forEach { delete(it.notionEntry) }
        getBlocked().first().forEach { delete(it.notionEntry) }
        insertAll(*entries.toTypedArray())
    }

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndSaveStaticResources(resources: List<String>, entries: List<NotionEntry>) {
        resources.forEach { deleteStaticResources(it) }
        insertAll(*entries.toTypedArray())
    }

    @Query("DELETE FROM notionEntry WHERE type = :type")
    suspend fun deleteStaticResources(type: String)

    @Delete
    suspend fun delete(model: NotionEntry)
}
