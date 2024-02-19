package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockParagraphDao {

    @Query("SELECT * FROM blockparagraph WHERE block_id =:blockId")
    fun getBlock(blockId: String): Flow<List<BlockParagraph>>

    @Insert
    suspend fun insertAll(vararg entries: BlockParagraph)

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndInsertAll(blockId: String, entries: List<BlockParagraph>) {
        deleteBlock(blockId)
        insertAll(*entries.toTypedArray())
    }

    @Query("DELETE FROM blockparagraph WHERE block_id=:blockId")
    suspend fun deleteBlock(blockId: String)
}
