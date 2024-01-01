package com.dbottillo.lifeos.feature.home

import com.dbottillo.lifeos.data.NextAction
import com.dbottillo.lifeos.data.NextActions
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
