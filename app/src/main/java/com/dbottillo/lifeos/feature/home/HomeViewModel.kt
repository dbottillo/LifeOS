package com.dbottillo.lifeos.feature.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.Blocked
import com.dbottillo.lifeos.feature.tasks.Focus
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Idea
import com.dbottillo.lifeos.feature.tasks.Inbox
import com.dbottillo.lifeos.feature.tasks.Project
import com.dbottillo.lifeos.feature.tasks.Resource
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.tasks.TasksState
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.notification.NotificationProvider
import com.dbottillo.lifeos.network.RefreshProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.dbottillo.lifeos.util.combine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val articleRepository: ArticleRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val articleManager: ArticleManager,
    private val logsRepository: LogsRepository,
    private val goalsRepository: GoalsRepository,
    private val widgetsRefresher: WidgetsRefresher
) : ViewModel() {

    private val otherStateBottomSelection = MutableStateFlow(
        BottomSelection.IDEAS
    )

    val homeState = MutableStateFlow(
        HomeState(
            refreshing = false,
            inbox = emptyList(),
            focus = emptyList(),
            blocked = emptyList(),
            projects = emptyList(),
            goals = emptyList(),
            others = HomeStateBottom(
                selection = listOf(),
                list = emptyList()
            ),
        )
    )

    val articleState = MutableStateFlow(
        ArticleScreenState(
            articles = Articles(emptyList(), emptyList()),
        )
    )

    val statusState = MutableStateFlow(
        StatusScreenState(
            tasksState = TasksState.Idle,
            workInfo = emptyList(),
            logs = emptyList()
        )
    )

    init {
        viewModelScope.launch {
            initHome()
        }
        viewModelScope.launch {
            initArticles()
        }
        viewModelScope.launch {
            initStatus()
        }
        refreshProvider.start()
    }

    @Suppress("LongMethod")
    private suspend fun initHome() {
        combine(
            combine(
                tasksRepository.focusFlow,
                tasksRepository.inboxFlow,
                tasksRepository.blockedFlow
            ) { focus, inbox, blocked ->
                Triple(focus, inbox, blocked)
            },
            tasksRepository.projectsFlow,
            tasksRepository.areasFlow,
            tasksRepository.ideasFlow,
            tasksRepository.resourcesFlow,
            otherStateBottomSelection,
            goalsRepository.goalsFlow
        ) { focusInboxBlocked, projects, areas, ideas, resources, bottomSelection, goals ->
            val uiAreas = areas.mapAreas()
            val uiResources = resources.mapResources()
            val uiIdeas = ideas.mapIdeas()
            val bottom = HomeStateBottom(
                selection = listOf(
                    HomeBottomSelection(
                        title = "Ideas (${uiIdeas.size})",
                        selected = bottomSelection == BottomSelection.IDEAS,
                        type = BottomSelection.IDEAS
                    ),
                    HomeBottomSelection(
                        title = "Areas (${uiAreas.size})",
                        selected = bottomSelection == BottomSelection.AREAS,
                        type = BottomSelection.AREAS
                    ),
                    HomeBottomSelection(
                        title = "Resources (${uiResources.size})",
                        selected = bottomSelection == BottomSelection.RESOURCES,
                        type = BottomSelection.RESOURCES
                    )
                ),
                list = when (bottomSelection) {
                    BottomSelection.AREAS -> uiAreas
                    BottomSelection.RESOURCES -> uiResources
                    BottomSelection.IDEAS -> uiIdeas
                }
            )
            Triple(
                focusInboxBlocked.second.mapInbox() to focusInboxBlocked.first.mapFocus(),
                focusInboxBlocked.third.mapBlocked() to projects.filter { it.status is Status.Focus }.mapProjects(),
                bottom to goals.filter { it.status is Status.Focus }.mapGoals()
            )
        }.collectLatest { (top, middle, bottom) ->
            homeState.value = homeState.first().copy(
                inbox = top.first,
                focus = top.second,
                blocked = middle.first,
                projects = middle.second,
                others = bottom.first,
                goals = bottom.second
            )
        }
    }

    private suspend fun initArticles() {
        articleRepository.articles().collectLatest { list ->
            articleState.value = articleState.first().copy(
                articles = Articles(
                    inbox = list.filter { !it.longRead },
                    longRead = list.filter { it.longRead }
                )
            )
        }
    }

    private suspend fun initStatus() {
        combine(
            tasksRepository.state,
            refreshProvider.workManagerStatus(),
            articleManager.status(),
            logsRepository.entries()
        ) { appState, workManagerStatus, articleManagerStatus, logs ->
            Triple(appState, logs, workManagerStatus to articleManagerStatus)
        }.collectLatest {
            statusState.value = statusState.first().copy(
                tasksState = it.first,
                workInfo = it.third.first + it.third.second.filter { it.state != WorkInfo.State.SUCCEEDED },
                logs = it.second
            )
        }
    }

    fun load() {
        refreshProvider.immediate()
    }

    fun stop() {
        notificationProvider.clear()
        refreshProvider.stop()
    }

    fun delete(article: Article) {
        viewModelScope.launch {
            articleRepository.deleteArticle(article)
        }
    }

    fun clear() {
        viewModelScope.launch {
            articleManager.clear()
        }
    }

    fun markAsRead(article: Article) {
        viewModelScope.launch {
            articleRepository.markArticleAsRead(article)
        }
    }

    fun reloadHome() {
        viewModelScope.launch {
            homeState.value = homeState.first().copy(
                refreshing = true
            )
            awaitAll(
                async { tasksRepository.loadNextActions() },
                async {
                    tasksRepository.loadStaticResources(
                        listOf("Project")
                    )
                },
                async {
                    tasksRepository.loadStaticResources(
                        listOf("Area", "Goal",)
                    )
                },
                async {
                    tasksRepository.loadStaticResources(
                        listOf("Idea")
                    )
                }
            )
            widgetsRefresher.refreshAll()
            homeState.value = homeState.first().copy(
                refreshing = false
            )
        }
    }

    private fun refreshResources() {
        viewModelScope.launch {
            tasksRepository.loadStaticResources(
                listOf("Resource")
            )
        }
    }

    fun bottomSelection(type: BottomSelection) {
        otherStateBottomSelection.value = type
    }

    fun bottomSelectionDoubleTap(type: BottomSelection) {
        if (type == BottomSelection.RESOURCES) {
            refreshResources()
        }
    }

    fun refreshProjects() {
        viewModelScope.launch {
            tasksRepository.loadStaticResources(
                listOf("Project")
            )
        }
    }
}

data class HomeState(
    val refreshing: Boolean,
    val inbox: List<EntryContent>,
    val blocked: List<EntryContent>,
    val focus: List<EntryContent>,
    val projects: List<EntryContent>,
    val goals: List<EntryContent>,
    val others: HomeStateBottom,
)

data class HomeStateBottom(
    val selection: List<HomeBottomSelection>,
    val list: List<EntryContent>
)

data class HomeBottomSelection(
    val title: String,
    val selected: Boolean,
    val type: BottomSelection
)

enum class BottomSelection {
    AREAS,
    RESOURCES,
    IDEAS
}

data class ArticleScreenState(
    val articles: Articles,
)

data class StatusScreenState(
    val tasksState: TasksState,
    val workInfo: List<WorkInfo>,
    val logs: List<Log>
)

data class Articles(val inbox: List<Article>, val longRead: List<Article>)

data class EntryContent(
    val id: String,
    val title: String,
    val url: String,
    val subtitle: String? = null,
    val link: String? = null,
    val parent: String? = null,
    val color: Color
)

fun List<Inbox>.mapInbox(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Focus>.mapFocus(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Project>.mapProjects(): List<EntryContent> {
    return map {
        val subtitle = it.progress?.let { "${(it * 100).toInt()}%" } ?: ""
        EntryContent(
            id = it.id,
            title = it.text,
            subtitle = subtitle,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Green.color
        )
    }
}

fun List<Blocked>.mapBlocked(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            subtitle = it.dueFormatted,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Red.color
        )
    }
}

fun List<Area>.mapAreas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link,
            color = ColorType.Yellow.color
        )
    }
}

fun List<Idea>.mapIdeas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Orange.color
        )
    }
}

fun List<Resource>.mapResources(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Purple.color
        )
    }
}

fun List<Goal>.mapGoals(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            parent = it.parent?.title,
            color = ColorType.Aqua.color
        )
    }
}

fun String?.toColor(): Color {
    return when (this) {
        "gray" -> ColorType.Gray.color
        "orange" -> ColorType.Orange.color
        "green" -> ColorType.Green.color
        "blue" -> ColorType.Blue.color
        "red" -> ColorType.Red.color
        "purple" -> ColorType.Purple.color
        "pink" -> ColorType.Pink.color
        "yellow" -> ColorType.Yellow.color
        else -> ColorType.Gray.color
    }
}

@Suppress("MagicNumber")
enum class ColorType(val color: Color) {
    Gray(Color(0xFF777777)),
    Orange(Color(0xFF572F0E)),
    Green(Color(0xFF2C6845)),
    Blue(Color(0xFF183A69)),
    Red(Color(0xFF751E1E)),
    Purple(Color(0xFF634681)),
    Pink(Color(0xFF8B1E77)),
    Yellow(Color(0xFF684511)),
    Aqua(Color(0xFF00535D)),
}
