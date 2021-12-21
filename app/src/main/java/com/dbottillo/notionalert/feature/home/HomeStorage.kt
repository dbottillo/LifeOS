package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.NextAction
import com.dbottillo.notionalert.NextActions
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface HomeStorage {
    suspend fun updateTimestamp()
    suspend fun updateNextActions(nextActions: List<NextAction>)

    val timestamp: Flow<OffsetDateTime>
    val nextActionsFlow: Flow<NextActions>
}

data class StorageInfo(
    val timeStamp: OffsetDateTime?
)
