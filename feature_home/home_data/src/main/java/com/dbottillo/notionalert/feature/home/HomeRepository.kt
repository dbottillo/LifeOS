package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.FilterRequest
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.NotionDatabase
import com.dbottillo.notionalert.NotionPage
import kotlinx.coroutines.async
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
        val mainPage = async { fetchMainPage() }
        val nextActions = async { fetchNextActions() }
        processResults(mainPage.await(), nextActions.await())
        processDatabaseResult(fetchNextActions())
    }

    private suspend fun processResults(
        mainPageResult: ApiResult<String>,
        databaseResult: ApiResult<String>
    ) {
        when (mainPageResult) {
            is ApiResult.Success -> {
                storage.saveMainPage(mainPageResult.data)
                // notificationProvider.updateMainPage(mainPageResult.data)
                processDatabaseResult(databaseResult)
            }
            is ApiResult.Error -> state.emit(
                AppState.Error(
                    mainPageResult.exception.localizedMessage,
                    storage.timestamp.first()
                )
            )
        }
    }

    private suspend fun processDatabaseResult(databaseResult: ApiResult<String>) {
        when (databaseResult) {
            is ApiResult.Success -> {
                storage.saveNextActions(databaseResult.data)
                notificationProvider.updateNextActions(databaseResult.data)
                state.emit(AppState.Loaded(storage.timestamp.first()))
            }
            is ApiResult.Error -> state.emit(
                AppState.Error(
                    databaseResult.exception.localizedMessage,
                    storage.timestamp.first()
                )
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchMainPage(): ApiResult<String> {
        return try {
            val response = api.getPage(MAIN_NOTION_PAGE_ID)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiResult.Success(parseMainPage(body))
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }

    private fun parseMainPage(notionPage: NotionPage): String {
        val nameProperty = notionPage.properties["Name"]
        val notionTitle = nameProperty?.title?.get(0)
            ?: throw UnsupportedOperationException("notion title is null")
        return notionTitle.plainText.replace("\n\n", " ").replace("\n", " ")
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchNextActions(): ApiResult<String> {
        return try {
            val response = api.queryDatabase(GTD_ONE_DATABASE_ID, FilterRequest())
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiResult.Success(parseNextActions(body))
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }

    private fun parseNextActions(database: NotionDatabase): String {
        val titles: List<String?> =
            database.results.map { it.properties["Name"]?.title?.get(0)?.plainText }
        return titles.filterNotNull().joinToString("\n")
    }

    suspend fun init() {
        val info = storage.data.first()
        notificationProvider.updateNextActions(info.nextActions)
        // notificationProvider.updateMainPage(info.mainPage)
        info.timeStamp?.let { state.emit(AppState.Restored(it)) }
    }
}

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
    data class Restored(val timestamp: OffsetDateTime) : AppState()
}

private const val MAIN_NOTION_PAGE_ID = "4be491b5ee164e299fa1f819825732be"
private const val GTD_ONE_DATABASE_ID = "1ecf1aad5b75430686cb91676942e5f1"
