package com.dbottillo.lifeos.network

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface RefreshProvider {
    fun start()
    fun stop()
    fun immediate()
    fun workManagerStatus(): Flow<List<WorkInfo>>
}
