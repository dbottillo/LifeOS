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
import com.dbottillo.lifeos.network.NotionDatabaseQueryResult
import com.dbottillo.lifeos.network.NotionPage
import com.dbottillo.lifeos.notification.NotificationProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Response
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
            val projectsAreasAndResourcesRequest = async { fetchProjectsAreaAndResources() }
            val ideasRequest = async { fetchIdeas() }
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
                    val results = (projectsAreasAndResources as ApiResult.Success).data.results +
                            (ideas as ApiResult.Success).data.results
                    dao.deleteAndInsertAllProjects(results.map { it.toEntry() })
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod", "StringLiteralDuplication")
    private suspend fun fetchNextActions(): ApiResult<NotionDatabaseQueryResult> {
        return try {
            val now = Instant.now()
            val dtm = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
            val request = NextActionsRequest(dtm.format(now))
            val response = api.queryDatabase(AppConstant.GTD_ONE_DATABASE_ID, request.get())
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

    private suspend fun storeAndNotify(
        result: NotionDatabaseQueryResult
    ) {
        dao.deleteAndInsertAll(result.results.map { it.toEntry(typeOverride = "alert") })
        val titles =
            result.results.map { page ->
                val name = page.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "No title"
                val emoji = page.icon?.emoji ?: ""
                emoji + name
            }
        val notificationData = titles.joinToString("\n")
        notificationProvider.updateNextActions(notificationData)
        state.emit(TasksState.Loaded(storage.timestamp.first()))
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod", "StringLiteralDuplication")
    private suspend fun fetchProjectsAreaAndResources(): ApiResult<NotionDatabaseQueryResult> {
        return try {
            val request = ProjectsAreasAndResourcesRequest()
            val response = api.queryDatabase(AppConstant.GTD_ONE_DATABASE_ID, request.get())
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

    @Suppress("TooGenericExceptionCaught", "LongMethod", "StringLiteralDuplication")
    private suspend fun fetchIdeas(): ApiResult<NotionDatabaseQueryResult> {
        return try {
            val request = IdeasRequest()
            val response = api.queryDatabase(AppConstant.GTD_ONE_DATABASE_ID, request.get())
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

    suspend fun addTask(databaseId: String, title: String?, url: String): Response<Any> {
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

sealed class TasksState {
    data object Idle : TasksState()
    data object Loading : TasksState()
    data class Loaded(val timestamp: OffsetDateTime) : TasksState()
    data class Error(val message: String, val timestamp: OffsetDateTime) : TasksState()
    data class Restored(val timestamp: OffsetDateTime) : TasksState()
}

private fun NotionPage.toEntry(typeOverride: String? = null) = NotionEntry(
    uid = id,
    color = properties["Type"]?.multiSelect?.joinToString(",") { it.color },
    title = properties["Name"]?.title?.getOrNull(0)?.plainText,
    url = url,
    emoji = icon?.emoji,
    type = typeOverride ?: properties["Category"]?.select?.name ?: "",
    startDate = properties["Due"]?.date?.start,
    endDate = properties["Due"]?.date?.end,
    timeZone = properties["Due"]?.date?.timeZone,
    status = properties["Status"]!!.status!!.name,
    link = properties["URL"]?.url
)
