package com.dbottillo.notionalert.feature.home

import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface HomeStorage {
    suspend fun updateTimestamp()
    suspend fun saveMainPage(data: String)
    suspend fun saveNextActions(data: String)

    val timestamp: Flow<OffsetDateTime>
    val data: Flow<StorageInfo>
}

data class StorageInfo(
    val nextActions: String,
    val mainPage: String,
    val timeStamp: OffsetDateTime
)
