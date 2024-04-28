package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.feature.home.HomeStorage
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val api: ApiInterface,
    private val storage: HomeStorage,
    private val notificationProvider: NotificationProvider,
    private val db: AppDatabase,
    private val mapper: TasksMapper
) {

    val state = MutableStateFlow<TasksState>(TasksState.Idle)

    private val dao by lazy { db.notionEntryDao() }

    val nextActionsFlow: Flow<List<NextAction>> = dao.getNextActions().map(mapper::mapNextActions)
    val projectsFlow: Flow<List<Project>> = dao.getProjects().map(mapper::mapProjects)
    val areasFlow: Flow<List<Area>> = dao.getAreas().map(mapper::mapAreas)
    val ideasFlow: Flow<List<Idea>> = dao.getIdeas().map(mapper::mapIdeas)
    val resourcesFlow: Flow<List<Resource>> = dao.getResources().map(mapper::mapResources)

    suspend fun init() {
        val titles = dao.getNextActions().first().joinToString("\n") {
            val name = it.title ?: "No title"
            val emoji = it.emoji ?: ""
            emoji + name
        }
        notificationProvider.updateNextActions(titles)
        storage.timestamp.first().let { state.emit(TasksState.Restored(it)) }
    }

    suspend fun loadNextActions() {
        when (val nextActions = fetchNextActions()) {
            is ApiResult.Success -> {
                storeAndNotify(nextActions.data)
            }

            is ApiResult.Error -> state.emit(
                TasksState.Error(
                    nextActions.exception.localizedMessage ?: "",
                    storage.timestamp.first()
                )
            )
        }
    }

    suspend fun loadProjectsAreaResourcesAndIdeas() {
        coroutineScope {
            val projectsAreasAndResourcesRequest = async {
                fetchNotionPages(ProjectsAreasAndResourcesRequest().get())
            }
            val ideasRequest = async {
                fetchNotionPages(IdeasRequest().get())
            }
            val projectsAreasAndResources = projectsAreasAndResourcesRequest.await()
            val ideas = ideasRequest.await()
            when {
                projectsAreasAndResources is ApiResult.Error -> state.emit(
                    TasksState.Error(
                        projectsAreasAndResources.exception.localizedMessage ?: "",
                        storage.timestamp.first()
                    )
                )

                ideas is ApiResult.Error -> state.emit(
                    TasksState.Error(
                        ideas.exception.localizedMessage ?: "",
                        storage.timestamp.first()
                    )
                )

                else -> {
                    val results = (projectsAreasAndResources as ApiResult.Success).data +
                            (ideas as ApiResult.Success).data
                    dao.deleteAndSaveAllProjectsAreaResourcesAndIdeas(results.map { it.toEntry() })
                }
            }
        }
    }

    private suspend fun fetchNextActions(): ApiResult<List<NotionPage>> {
        val now = Instant.now()
        val dtm = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        val request = NextActionsRequest(dtm.format(now))
        return fetchNotionPages(request.get())
    }

    private suspend fun storeAndNotify(
        result: List<NotionPage>
    ) {
        dao.deleteAndInsertAll(result.map { it.toEntry(typeOverride = "alert") })
        val titles =
            result.map { page ->
                val name = page.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "No title"
                val emoji = page.icon?.emoji ?: ""
                emoji + name
            }
        val notificationData = titles.joinToString("\n")
        notificationProvider.updateNextActions(notificationData)
        state.emit(TasksState.Loaded(storage.timestamp.first()))
    }

    private suspend fun fetchNotionPages(request: NotionBodyRequest): ApiResult<List<NotionPage>> {
        val result = mutableListOf<NotionPage>()
        var nextRequest: NotionBodyRequest? = request
        while (nextRequest != null) {
            nextRequest = when (val queryResult = networkRequest(nextRequest)) {
                is ApiResult.Error -> return queryResult
                is ApiResult.Success -> {
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
}

sealed class TasksState {
    data object Idle : TasksState()
    data object Loading : TasksState()
    data class Loaded(val timestamp: OffsetDateTime) : TasksState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : TasksState()
    data class Restored(val timestamp: OffsetDateTime) : TasksState()
}

private fun NotionPage.toEntry(typeOverride: String? = null) = NotionEntry(
    uid = id,
    color = properties["Tag"]?.multiSelect?.joinToString(",") { it.color },
    title = properties["Name"]?.title?.getOrNull(0)?.plainText,
    url = url,
    emoji = icon?.emoji,
    type = typeOverride ?: properties["Type"]?.select?.name ?: "",
    startDate = properties["Due"]?.date?.start,
    endDate = properties["Due"]?.date?.end,
    timeZone = properties["Due"]?.date?.timeZone,
    progress = properties["Progress"]?.rollup?.number,
    status = properties["Status"]!!.status!!.name,
    link = properties["URL"]?.url
)
