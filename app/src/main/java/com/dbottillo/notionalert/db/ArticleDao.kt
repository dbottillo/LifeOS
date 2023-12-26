package com.dbottillo.notionalert.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM article")
    fun getAll(): Flow<List<Article>>

    @Insert
    suspend fun insertAll(vararg articles: Article)

    @Suppress("SpreadOperator")
    @Transaction
    suspend fun deleteAndInsertAll(articles: List<Article>) {
        delete()
        insertAll(*articles.toTypedArray())
    }

    @Query("DELETE FROM article")
    suspend fun delete()
}
