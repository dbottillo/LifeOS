package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.OffsetDateTime
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
        val sortedActions = result.results.sortedBy { it.icon == null }
        val nextActions = sortedActions.map { page ->
            val name = page.properties["Name"]?.title?.get(0)?.plainText
            val emoji = page.icon?.emoji ?: ""
            val text = emoji + name
            NextAction.newBuilder().setColor(
                page.properties["Type"]?.multiSelect!!.joinToString(",") { it.color }
            ).setText(text).build()
        }
        val titles =
            sortedActions.map { page ->
                val name = page.properties["Name"]?.title?.get(0)?.plainText
                val emoji = page.icon?.emoji ?: ""
                emoji + name
            }
        val notificationData = titles.joinToString("\n")
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
        val authorizationCode = pocketStorage.authorizationCodeFlow.first()
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

private const val GTD_ONE_DATABASE_ID = "1ecf1aad5b75430686cb91676942e5f1"
