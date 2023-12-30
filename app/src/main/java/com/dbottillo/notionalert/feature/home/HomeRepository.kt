package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.data.AppConstant
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
import com.dbottillo.notionalert.feature.articles.ArticleManager
import com.dbottillo.notionalert.network.AddPageNotionBodyRequest
import com.dbottillo.notionalert.network.AddPageNotionBodyRequestParent
import com.dbottillo.notionalert.network.AddPageNotionProperty
import com.dbottillo.notionalert.network.AddPageNotionPropertyText
import com.dbottillo.notionalert.network.AddPageNotionPropertyTitle
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val api: ApiInterface,
    private val storage: HomeStorage,
    private val notificationProvider: NotificationProvider,
    private val db: AppDatabase,
    private val articleManager: ArticleManager
) {

    val state = MutableStateFlow<AppState>(AppState.Idle)

    suspend fun loadNextActions() = coroutineScope {
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
            val response = api.queryDatabase(AppConstant.GTD_ONE_DATABASE_ID, request)
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
            val response = api.queryDatabase(AppConstant.ARTICLES_DATABASE_ID, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val articles = body.results.map {
                        Article(
                            uid = it.id,
                            url = it.properties["URL"]?.url ?: "",
                            longRead = it.properties["Status"]?.status?.name == "Long read",
                            title = it.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "",
                            status = "synced"
                        )
                    }
                    db.articleDao().deleteAndInsertAll(articles)
                }
            }
            val throwable = Throwable("${response.code()} ${response.message()}")
            Firebase.crashlytics.recordException(throwable)
            ApiResult.Error(throwable)
        } catch (e: Exception) {
            val throwable = Throwable(e.message ?: e.toString())
            Firebase.crashlytics.recordException(throwable)
            ApiResult.Error(throwable)
        }
    }

    fun articles(): Flow<List<Article>> {
        return db.articleDao().getAllSyncedArticles()
    }

    suspend fun deleteArticle(article: Article) {
        withContext(Dispatchers.IO) {
            db.articleDao().updateArticle(article.copy(status = "delete"))
            articleManager.deleteArticle(article)
        }
    }

    suspend fun markArticleAsRead(article: Article) {
        withContext(Dispatchers.IO) {
            db.articleDao().updateArticle(article.copy(status = "read"))
            articleManager.updateArticleStatus(article, "Done")
        }
    }

    suspend fun addPage(databaseId: String, title: String?, url: String): Response<Any> {
        return api.addPage(
            body = AddPageNotionBodyRequest(
                parent = AddPageNotionBodyRequestParent(
                    type = "database_id",
                    databaseId = databaseId
                ),
                properties = mapOf(
                    "Name" to AddPageNotionProperty(
                        title = listOf(
                            AddPageNotionPropertyTitle(
                                AddPageNotionPropertyText(content = title)
                            )
                        )
                    ),
                    "URL" to AddPageNotionProperty(
                        url = url
                    )
                )
            )
        )
    }
}

sealed class AppState {
    data object Idle : AppState()
    data object Loading : AppState()
    data class Loaded(val timestamp: OffsetDateTime) : AppState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : AppState()
    data class Restored(val timestamp: OffsetDateTime) : AppState()
}
