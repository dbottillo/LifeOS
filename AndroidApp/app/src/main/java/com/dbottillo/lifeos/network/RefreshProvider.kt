package com.dbottillo.lifeos.network

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface RefreshProvider {
    fun start()
    fun stop()
    fun immediate()
    fun periodicStatus(): Flow<List<WorkInfo>>
    fun dailyStatus(): Flow<List<WorkInfo>>
}
