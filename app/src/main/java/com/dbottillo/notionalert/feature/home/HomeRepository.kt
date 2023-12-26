package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.network.ApiInterface
import com.dbottillo.notionalert.network.ApiResult
import com.dbottillo.notionalert.network.FilterBeforeRequest
import com.dbottillo.notionalert.network.FilterCheckboxRequest
import com.dbottillo.notionalert.network.FilterRequest
import com.dbottillo.notionalert.network.FilterEqualsRequest
import com.dbottillo.notionalert.notification.NotificationProvider
import com.dbottillo.notionalert.network.NotionBodyRequest
import com.dbottillo.notionalert.network.NotionDatabaseQueryResult
import com.dbottillo.notionalert.network.SortRequest
import com.dbottillo.notionalert.data.NextAction
import com.dbottillo.notionalert.db.AppDatabase
import com.dbottillo.notionalert.db.Article
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: ApiInterface,
    private val storage: HomeStorage,
    private val notificationProvider: NotificationProvider,
    private val articlesStorage: ArticlesStorage,
    private val db: AppDatabase
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

    suspend fun fetchArticles() = coroutineScope {
        try {
            val request = NotionBodyRequest(
                filter = FilterRequest(
                    or = listOf(
                        FilterRequest(
                            property = "Status",
                            status = FilterEqualsRequest(
                                equals = "Inbox"
                            )
                        ),
                        FilterRequest(
                            property = "Status",
                            status = FilterEqualsRequest(
                                equals = "Long read"
                            )
                        )
                    )
                ),
                sorts = emptyList()
            )
            val response = api.queryDatabase(ARTICLES_DATABASE_ID, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val articles = body.results.map {
                        Article(
                            uid = it.id,
                            url = it.properties["URL"]?.url ?: "",
                            longRead = it.properties["Status"]?.status?.name == "Long read",
                            title = it.properties["Name"]?.title?.get(0)?.plainText ?: "",
                            synced = true
                        )
                    }
                    db.articleDao().deleteAndInsertAll(articles)
                    articlesStorage.updateNumberToRead(body.results.count())
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }

    suspend fun articles(): Flow<List<Article>> {
        return db.articleDao().getAll()
    }
}

sealed class AppState {
    data object Idle : AppState()
    data object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
    data class Restored(val timestamp: OffsetDateTime) : AppState()
}

const val GTD_ONE_DATABASE_ID = "1ecf1aad5b75430686cb91676942e5f1"
const val ARTICLES_DATABASE_ID = "ef1963ca16574555874f5c3dc2523b61"
