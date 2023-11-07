package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.BuildConfig
import com.dbottillo.notionalert.FilterBeforeRequest
import com.dbottillo.notionalert.FilterCheckboxRequest
import com.dbottillo.notionalert.FilterRequest
import com.dbottillo.notionalert.FilterEqualsRequest
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.NotionBodyRequest
import com.dbottillo.notionalert.NotionDatabaseQueryResult
import com.dbottillo.notionalert.PocketApiInterface
import com.dbottillo.notionalert.SortRequest
import com.dbottillo.notionalert.data.NextAction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: ApiInterface,
    private val pocketApi: PocketApiInterface,
    private val storage: HomeStorage,
    private val notificationProvider: NotificationProvider,
    private val pocketStorage: PocketStorage
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
        val nextActions = result.results.map { page ->
            val name = page.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "No title"
            val emoji = page.icon?.emoji ?: ""
            val text = emoji + name
            NextAction(
                color = page.properties["Type"]?.multiSelect!!.joinToString(",") { it.color },
                text = text,
                url = page.url
            )
        }
        val titles =
            result.results.map { page ->
                val name = page.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "No title"
                val emoji = page.icon?.emoji ?: ""
                emoji + name
            }
        val notificationData = titles.joinToString("\n")
        storage.updateNextActions(nextActions)
        notificationProvider.updateNextActions(notificationData)
        state.emit(AppState.Loaded(storage.timestamp.first()))
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod", "StringLiteralDuplication")
    private suspend fun fetchNextActions(): ApiResult<NotionDatabaseQueryResult> {
        return try {
            val now = Instant.now()
            val dtm = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
            val date = dtm.format(now)
            val request = NotionBodyRequest(
                filter = FilterRequest(
                    or = listOf(
                        FilterRequest(
                            and = listOf(
                                FilterRequest(
                                    property = "Due",
                                    date = FilterBeforeRequest(onOrBefore = date)
                                ),
                                FilterRequest(
                                    property = "Category",
                                    select = FilterEqualsRequest(
                                        equals = "Task"
                                    )
                                )
                            )
                        ),
                        FilterRequest(
                            and = listOf(
                                FilterRequest(
                                    property = "Status",
                                    status = FilterEqualsRequest(
                                        equals = "Focus"
                                    )
                                ),
                                FilterRequest(
                                    property = "Category",
                                    select = FilterEqualsRequest(
                                        equals = "Task"
                                    )
                                )
                            )
                        ),
                        FilterRequest(
                            and = listOf(
                                FilterRequest(
                                    property = "Status",
                                    status = FilterEqualsRequest(
                                        equals = "Inbox"
                                    )
                                ),
                                FilterRequest(
                                    property = "Favourite",
                                    checkbox = FilterCheckboxRequest(
                                        equals = false
                                    )
                                )
                            )
                        )
                    )
                ),
                sorts = listOf(
                    SortRequest(
                        property = "Favourite",
                        direction = "descending"
                    ),
                    SortRequest(
                        property = "Status",
                        direction = "ascending"
                    ),
                    SortRequest(
                        property = "Due",
                        direction = "ascending"
                    )
                )
            )
            val response = api.queryDatabase(GTD_ONE_DATABASE_ID, request)
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
            nextActions.actions.joinToString("\n") { it.text }
        notificationProvider.updateNextActions(titles)
        storage.timestamp.first().let { state.emit(AppState.Restored(it)) }
    }

    suspend fun getPocketToken(): ApiResult<String> {
        return try {
            val response = pocketApi.oauthRequest(
                consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
                redirectUri = "pocketapp104794:authorizationFinished"
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiResult.Success(body.string().split("=")[1])
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    suspend fun authorizePocket(code: String): ApiResult<Pair<String, String>> {
        return try {
            val response = pocketApi.oauthAuthorize(
                consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
                code = code
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val result = body.string().split("&")
                    val accessToken = result[0].split("=")[1]
                    val userName = result[1].split("=")[1]
                    return ApiResult.Success(accessToken to userName)
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

    suspend fun fetchPocketArticles() = coroutineScope {
        /*val authorizationCode = pocketStorage.authorizationCodeFlow.first()
        if (authorizationCode.isNotEmpty()) {
            try {
                val response = pocketApi.getArticles(
                    consumerKey = BuildConfig.POCKET_CONSUMER_KEY,
                    accessToken = authorizationCode
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        pocketStorage.updateNumberToRead(body.list.count())
                    } else {
                    }
                } else {
                }
            } catch (e: Exception) {
            }
        }*/
        try {
            val request = NotionBodyRequest(
                filter = FilterRequest(
                    property = "Read",
                    checkbox = FilterCheckboxRequest(equals = false)
                ),
                sorts = emptyList()
            )
            val response = api.queryDatabase(POCKET_DATABASE_ID, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    pocketStorage.updateNumberToRead(body.results.count())
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }
}

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
    data class Restored(val timestamp: OffsetDateTime) : AppState()
}

const val GTD_ONE_DATABASE_ID = "1ecf1aad5b75430686cb91676942e5f1"
const val POCKET_DATABASE_ID = "ef1963ca16574555874f5c3dc2523b61"
