package com.dbottillo.lifeos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.db.BlockParagraph
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.blocks.BlockRepository
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.Idea
import com.dbottillo.lifeos.feature.tasks.NextAction
import com.dbottillo.lifeos.feature.tasks.Project
import com.dbottillo.lifeos.feature.tasks.Resource
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.tasks.TasksState
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val articleRepository: ArticleRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val articleManager: ArticleManager,
    private val logsRepository: LogsRepository,
    private val blockRepository: BlockRepository
) : ViewModel() {

    private val otherStateBottomSelection = MutableStateFlow(
        BottomSelection.IDEAS
    )

    val homeState = MutableStateFlow(
        HomeState(
            refreshing = false,
            inbox = emptyList(),
            focus = emptyList(),
            projects = emptyList(),
            others = HomeStateBottom(
                selection = listOf(),
                list = emptyList()
            ),
            goals = emptyList()
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

    private suspend fun initHome() {
        combine(
            tasksRepository.nextActionsFlow,
            tasksRepository.projectsFlow,
            tasksRepository.areasFlow,
            tasksRepository.ideasFlow,
            tasksRepository.resourcesFlow,
            otherStateBottomSelection,
            blockRepository.goalsBlock()
        ) { actions, projects, areas, ideas, resources, bottomSelection, goalsParagraphs ->
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
            val (inbox, others) = actions.partition { it.isInbox }
            val (withDue, withoutDue) = others.partition { it.due.isNotEmpty() }
            Triple(
                (inbox + withDue).mapActions() to (withoutDue).mapActions(),
                projects.filter { it.status is Status.Focus }.mapProjects(),
                bottom to goalsParagraphs
            )
        }.collectLatest { (top, middle, bottom) ->
            homeState.value = homeState.first().copy(
                inbox = top.first,
                focus = top.second,
                projects = middle,
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
            tasksRepository.loadProjectsAreaResourcesAndIdeas() // projects need to have priority first
            tasksRepository.loadNextActions()
            blockRepository.loadGoals()
            homeState.value = homeState.first().copy(
                refreshing = false
            )
        }
    }

    fun bottomSelection(type: BottomSelection) {
        otherStateBottomSelection.value = type
    }
}

data class HomeState(
    val refreshing: Boolean,
    val inbox: List<EntryContent>,
    val focus: List<EntryContent>,
    val projects: List<EntryContent>,
    val others: HomeStateBottom,
    val goals: List<BlockParagraph>
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
    val link: String? = null
)

fun List<NextAction>.mapActions(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            subtitle = it.due,
            link = it.link
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
            link = it.link
        )
    }
}

fun List<Area>.mapAreas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link
        )
    }
}

fun List<Idea>.mapIdeas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link
        )
    }
}

fun List<Resource>.mapResources(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            link = it.link
        )
    }
}
