package com.dbottillo.lifeos.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM article ORDER BY created_at DESC")
    fun getAll(): Flow<List<Article>>

    @Query("SELECT * FROM article WHERE status='synced' ORDER BY created_at DESC")
    fun getAllSyncedArticles(): Flow<List<Article>>

    @Query("SELECT * FROM article WHERE status='delete' ORDER BY created_at DESC")
    fun getAllDeletedArticles(): Flow<List<Article>>

    @Query("SELECT * FROM article WHERE status='read' ORDER BY created_at DESC")
    fun getAllReadArticles(): Flow<List<Article>>

    @Query("SELECT * FROM article WHERE uid=:uuid")
    suspend fun findArticle(uuid: String): Article

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

    @Delete
    suspend fun deleteArticle(article: Article)

    @Update
    suspend fun updateArticle(article: Article)
}
