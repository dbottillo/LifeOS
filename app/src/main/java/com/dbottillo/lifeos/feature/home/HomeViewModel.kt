package com.dbottillo.lifeos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.articles.ArticleRepository
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
) : ViewModel() {

    private val homeStateBottomSelection = MutableStateFlow(
        BottomSelection.AREAS
    )

    val homeState = MutableStateFlow(
        HomeState(
            refreshing = false,
            top = emptyList(),
            middle = emptyList(),
            bottom = HomeStateBottom(
                selection = listOf(),
                list = emptyList()
            )
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
            homeStateBottomSelection
        ) { actions, projects, areas, ideas, resources, bottomSelection ->
            val bottom = HomeStateBottom(
                selection = listOf(
                    HomeBottomSelection(
                        title = "Areas",
                        selected = bottomSelection == BottomSelection.AREAS,
                        type = BottomSelection.AREAS
                    ),
                    HomeBottomSelection(
                        title = "Ideas",
                        selected = bottomSelection == BottomSelection.IDEAS,
                        type = BottomSelection.IDEAS
                    ),
                    HomeBottomSelection(
                        title = "Resources",
                        selected = bottomSelection == BottomSelection.RESOURCES,
                        type = BottomSelection.RESOURCES
                    )
                ),
                list = when (bottomSelection) {
                    BottomSelection.AREAS -> areas.mapAreas()
                    BottomSelection.RESOURCES -> resources.mapResources()
                    BottomSelection.IDEAS -> ideas.mapIdeas()
                }
            )
            Triple(
                actions.mapActions(),
                projects.filter { it.status is Status.Focus }.mapProjects(),
                bottom
            )
        }.collectLatest { (top, middle, bottom) ->
            homeState.value = homeState.first().copy(
                top = top,
                middle = middle,
                bottom = bottom
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
            homeState.value = homeState.first().copy(
                refreshing = false
            )
        }
    }

    fun bottomSelection(type: BottomSelection) {
        homeStateBottomSelection.value = type
    }
}

data class HomeState(
    val refreshing: Boolean,
    val top: List<EntryContent>,
    val middle: List<EntryContent>,
    val bottom: HomeStateBottom
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
    val subtitle: String? = null
)

fun List<NextAction>.mapActions(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url,
            subtitle = it.due
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
            url = it.url
        )
    }
}

fun List<Area>.mapAreas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url
        )
    }
}

fun List<Idea>.mapIdeas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url
        )
    }
}

fun List<Resource>.mapResources(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            title = it.text,
            url = it.url
        )
    }
}
