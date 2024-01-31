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
import com.dbottillo.lifeos.feature.tasks.NextAction
import com.dbottillo.lifeos.feature.tasks.Project
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val articleRepository: ArticleRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val articleManager: ArticleManager,
    private val logsRepository: LogsRepository,
) : ViewModel() {

    val homeState = MutableStateFlow<HomeState>(
        HomeState(
            refreshing = false,
            nextActions = emptyList(),
            projects = emptyList(),
            areas = emptyList()
        )
    )

    val articleState = MutableStateFlow<ArticleScreenState>(
        ArticleScreenState(
            articles = Articles(emptyList(), emptyList()),
        )
    )

    val statusState = MutableStateFlow<StatusScreenState>(
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
            tasksRepository.areasFlow
        ) { actions, projects, areas ->
            Triple(actions, projects, areas)
        }.collectLatest { (actions, projects, areas) ->
            homeState.value = homeState.first().copy(
                nextActions = actions,
                projects = projects.filter { it.status is Status.Focus },
                areas = areas
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
            tasksRepository.loadProjectsAreaAndIdeas() // projects need to have priority first
            tasksRepository.loadNextActions()
            homeState.value = homeState.first().copy(
                refreshing = false
            )
        }
    }
}

data class HomeState(
    val refreshing: Boolean,
    val nextActions: List<NextAction>,
    val projects: List<Project>,
    val areas: List<Area>
)

data class ArticleScreenState(
    val articles: Articles,
)

data class StatusScreenState(
    val tasksState: TasksState,
    val workInfo: List<WorkInfo>,
    val logs: List<Log>
)

data class Articles(val inbox: List<Article>, val longRead: List<Article>)
