package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.NotionPage
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

    suspend fun makeNetworkRequest() {
        state.emit(AppState.Loading)
        storage.updateTimestamp()
        when (val notionPageResult = fetchMainPage()) {
            is ApiResult.Success -> {
                notificationProvider.update(notionPageResult.data)
                state.emit(AppState.Loaded(storage.timestamp.first()))
            }
            is ApiResult.Error -> state.emit(
                AppState.Error(
                    notionPageResult.exception.localizedMessage,
                    storage.timestamp.first()
                )
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchMainPage(): ApiResult<NotionPage> {
        return try {
            val response = api.getPage(MAIN_NOTION_PAGE_ID)
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
}

private const val MAIN_NOTION_PAGE_ID = "4be491b5ee164e299fa1f819825732be"

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
}
