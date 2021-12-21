package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.NotionDatabaseQueryResult
import com.dbottillo.notionalert.NextAction
import com.dbottillo.notionalert.FilterRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.OffsetDateTime
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: ApiInterface,
    private val storage: HomeStorage,
    private val notificationProvider: NotificationProvider
) {

    val state = MutableStateFlow<AppState>(AppState.Idle)

    suspend fun makeNetworkRequest() = coroutineScope {
        state.emit(AppState.Loading)
        storage.updateTimestamp()
        val nextActions = fetchNextActions()
        processDatabaseResult(nextActions)
    }

    private suspend fun processDatabaseResult(
        databaseResult: ApiResult<NotionDatabaseQueryResult>
    ) {
        when (databaseResult) {
            is ApiResult.Success -> {
                storeAndNotify(databaseResult.data)
            }
            is ApiResult.Error -> state.emit(
                AppState.Error(
                    databaseResult.exception.localizedMessage ?: "",
                    storage.timestamp.first()
                )
            )
        }
    }

    private suspend fun storeAndNotify(
        result: NotionDatabaseQueryResult
    ) {
        val nextActions = result.results.map {
            NextAction.newBuilder().setColor(
                it.properties["Type"]?.multiSelect!!.map { it.color }.joinToString(",")
            ).setText(
                it.properties["Name"]?.title?.get(0)?.plainText
            ).build()
        }
        val titles =
            result.results.map { it.properties["Name"]?.title?.get(0)?.plainText }
        val notificationData = titles.filterNotNull().joinToString("\n")
        storage.updateNextActions(nextActions)
        notificationProvider.updateNextActions(notificationData)
        state.emit(AppState.Loaded(storage.timestamp.first()))
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchNextActions(): ApiResult<NotionDatabaseQueryResult> {
        return try {
            val response = api.queryDatabase(GTD_ONE_DATABASE_ID, FilterRequest())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiResult.Success(body)
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }

    suspend fun init() {
        val nextActions = storage.nextActionsFlow.first()
        val titles =
            nextActions.actionsOrBuilderList.joinToString("\n") { it.text }
        notificationProvider.updateNextActions(titles)
        storage.timestamp.first().let { state.emit(AppState.Restored(it)) }
    }
}

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
    data class Restored(val timestamp: OffsetDateTime) : AppState()
}

private const val GTD_ONE_DATABASE_ID = "1ecf1aad5b75430686cb91676942e5f1"
