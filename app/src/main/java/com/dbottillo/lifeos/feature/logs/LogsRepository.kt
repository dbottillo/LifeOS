package com.dbottillo.lifeos.feature.logs

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogsRepository @Inject constructor(val db: AppDatabase) {

    private val dao by lazy { db.logDao() }

    fun entries(): Flow<List<Log>> = dao.getAll()

    suspend fun addEntry(tag: LogTags, level: LogLevel, message: String) {
        withContext(Dispatchers.IO) {
            dao.insert(
                Log(
                id = 0,
                tag = tag.key,
                level = level.key,
                message = message,
                createdAt = System.currentTimeMillis()
            )
            )
        }
    }
}

enum class LogTags(val key: String) {
    ADD_ARTICLE_WORKER("add_article_worker"),
    DELETE_ARTICLE_WORKER("delete_article_worker"),
    UPDATE_ARTICLE_WORKER("update_article_worker")
}

enum class LogLevel(val key: String) {
    INFO("info"),
    DEBUG("debug"),
    ERROR("error")
}
