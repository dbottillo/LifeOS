package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM log ORDER BY createdAt DESC LIMIT 100")
    fun getAll(): Flow<List<Log>>

    @Insert
    suspend fun insert(log: Log)

    @Query("DELETE from log where createdAt<=:timeStampOfNinetyDays")
    suspend fun deleteOldLogs(timeStampOfNinetyDays: Long)
}
