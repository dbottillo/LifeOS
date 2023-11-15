package com.dbottillo.notionalert.feature.home

import kotlinx.coroutines.flow.Flow

interface ArticlesStorage {
    suspend fun updateNumberToRead(total: Int)
    val numberToReadFlow: Flow<Int>
}
