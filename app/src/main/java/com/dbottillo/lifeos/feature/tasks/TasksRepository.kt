package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.network.AddPageNotionBodyRequest
import com.dbottillo.lifeos.network.AddPageNotionBodyRequestParent
import com.dbottillo.lifeos.network.AddPageNotionProperty
import com.dbottillo.lifeos.network.AddPageNotionPropertyText
import com.dbottillo.lifeos.network.AddPageNotionPropertyTitle
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.ApiResult
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.NotionDatabaseQueryResult
import com.dbottillo.lifeos.network.NotionPage
import com.dbottillo.lifeos.notification.NotificationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val api: ApiInterface,
    private val notificationProvider: NotificationProvider,
    private val db: AppDatabase,
    private val mapper: TasksMapper,
    private val logsRepository: LogsRepository
) {

    private val dao by lazy { db.notionEntryDao() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val inboxFlow: Flow<List<Inbox>> = dao.getInbox().map(mapper::mapInbox).mapLatest {
        it.filter { entry ->
            if (entry.due == null) {
                true
            } else {
                val today = LocalDate.now()
                val date = entry.due.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                date == today || date.isBefore(today)
            }
        }
    }
    val focusFlow: Flow<List<Focus>> = dao.getFocus().map(mapper::mapFocus)
    val blockedFlow: Flow<List<Blocked>> = dao.getBlocked().map(mapper::mapBlocked)
    val projectsFlow: Flow<List<Project>> = dao.getProjects().map(mapper::mapProjects)
    val areasFlow: Flow<List<Area>> = dao.getAreas().map(mapper::mapAreas)
    val ideasFlow: Flow<List<Idea>> = dao.getIdeas().map(mapper::mapIdeas)
    val resourcesFlow: Flow<List<Resource>> = dao.getResources().map(mapper::mapResources)

    suspend fun init() {
        updateFocusNotification()
    }

    suspend fun loadNextActions(): Result<Unit> {
        logsRepository.addEntry(
            LogTags.HOME_REFRESH,
            LogLevel.INFO,
            "load next actions"
        )
        return when (val nextActions = fetchFocusInboxBlocked()) {
            is ApiResult.Success -> {
                storeAndNotify(nextActions.data)
                logsRepository.addEntry(
                    LogTags.HOME_REFRESH,
                    LogLevel.INFO,
                    "successfully loaded next actions"
                )
                Result.success(Unit)
            }

            is ApiResult.Error -> {
                logsRepository.addEntry(
                    LogTags.HOME_REFRESH,
                    LogLevel.ERROR,
                    nextActions.exception.localizedMessage ?: "no error message"
                )
                Result.failure(nextActions.exception)
            }
        }
    }

    suspend fun loadStaticResources(resources: List<String>): Result<Unit> {
        logsRepository.addEntry(
            LogTags.HOME_REFRESH,
            LogLevel.INFO,
            "load static resources"
        )
        return coroutineScope {
            val pages = fetchNotionPages(StaticResourcesRequest().get(resources = resources))
            when (pages) {
                is ApiResult.Error -> {
                    logsRepository.addEntry(
                        LogTags.HOME_REFRESH,
                        LogLevel.ERROR,
                        "failed to load $resources -> ${pages.exception}"
                    )
                    Result.failure(pages.exception)
                }
                is ApiResult.Success -> {
                    val results = pages.data
                    val entries = results.map { it.toEntry() }
                    dao.deleteAndSaveStaticResources(resources, entries)
                    logsRepository.addEntry(
                        LogTags.HOME_REFRESH,
                        LogLevel.INFO,
                        "successfully loaded $resources"
                    )
                    Result.success(Unit)
                }
            }
        }
    }

    private suspend fun fetchFocusInboxBlocked(): ApiResult<List<NotionPage>> {
        val now = Instant.now()
        val dtm = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        val request = FocusInboxBlockedRequest(dtm.format(now))
        return fetchNotionPages(request.get())
    }

    private suspend fun storeAndNotify(
        result: List<NotionPage>
    ) {
        dao.deleteAndSaveFocusInboxBlocked(result.map { it.toEntry() })
        updateFocusNotification()
    }

    private suspend fun fetchNotionPages(request: NotionBodyRequest): ApiResult<List<NotionPage>> {
        val result = mutableListOf<NotionPage>()
        var nextRequest: NotionBodyRequest? = request
        while (nextRequest != null) {
            nextRequest = when (val queryResult = networkRequest(nextRequest)) {
                is ApiResult.Error -> return queryResult
                is ApiResult.Success -> {
                    result.addAll(queryResult.data.results)
                    result.addAll(queryResult.data.results)
                    if (queryResult.data.nextCursor != null) {
                        request.copy(
                            startCursor = queryResult.data.nextCursor,
                        )
                    } else {
                        null
                    }
                }
            }
        }
        return ApiResult.Success(result)
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod", "StringLiteralDuplication")
    private suspend fun networkRequest(request: NotionBodyRequest): ApiResult<NotionDatabaseQueryResult> {
        return try {
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

    suspend fun addTask(databaseId: String, title: String?, url: String): ApiResult<Unit> {
        return try {
            val request = AddPageNotionBodyRequest(
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
            val response = api.addPage(body = request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return ApiResult.Success(Unit)
                }
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }

    private suspend fun updateFocusNotification() {
        val actions = inboxFlow.first()
        if (actions.isEmpty()) {
            notificationProvider.clear()
        } else {
            val titles = actions.joinToString("\n") {
                it.text
            }
            notificationProvider.updateNextActions(titles)
        }
    }
}

private fun NotionPage.toEntry() = NotionEntry(
    uid = id,
    color = (properties["Type"]?.select?.name ?: "").mapColor(),
    title = properties["Name"]?.title?.getOrNull(0)?.plainText,
    url = url,
    emoji = icon?.emoji,
    type = properties["Type"]?.select?.name ?: "",
    startDate = properties["Due"]?.date?.start,
    endDate = properties["Due"]?.date?.end,
    timeZone = properties["Due"]?.date?.timeZone,
    progress = properties["Progress"]?.rollup?.number,
    status = properties["Status"]!!.status!!.name,
    link = properties["URL"]?.url,
    parentId = properties["Parent item"]?.relation?.firstOrNull()?.id
)

fun String.mapColor(): String {
    return when (this) {
        "Idea" -> "orange"
        "Task" -> "blue"
        "Resource" -> "purple"
        "Project" -> "green"
        "Area" -> "yellow"
        "Bookmark" -> "pink"
        else -> "gray"
    }
}
