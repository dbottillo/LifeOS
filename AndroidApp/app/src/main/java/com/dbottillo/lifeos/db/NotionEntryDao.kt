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
    @Query("SELECT * FROM notionEntry WHERE status = 'Focus' and type !='Goal' and type !='Folder' order by parentId")
    fun getFocus(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE status = 'Next week' order by parentId")
    fun getNextWeek(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Folder'")
    fun getFolders(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Area'")
    fun getAreas(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Resource'")
    fun getResources(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Query("SELECT * FROM notionEntry WHERE type = 'Goal'")
    fun getGoals(): Flow<List<NotionEntryWithParent>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg entries: NotionEntry)

    @Query("SELECT * FROM notionEntry WHERE uid = :entryId")
    suspend fun getEntry(entryId: String): NotionEntryWithParent

    @Query(
        "UPDATE notionEntry SET title = :title, link = :link, type = :type, status = :status, start_date = :startDate WHERE uid = :entryId"
    )
    suspend fun updateEntry(
        entryId: String,
        title: String?,
        link: String?,
        type: String?,
        status: String,
        startDate: String?
    ): Int

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndSaveFocusInboxNextWeek(entries: List<NotionEntry>) {
        getInbox().first().forEach { delete(it.notionEntry) }
        getFocus().first().forEach { delete(it.notionEntry) }
        getNextWeek().first().forEach { delete(it.notionEntry) }
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
