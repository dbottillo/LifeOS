package com.dbottillo.lifeos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.tasks.TasksState
import com.dbottillo.lifeos.feature.workers.WorkerRepository
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
    private val workerRepository: WorkerRepository
) : ViewModel() {

    val state = MutableStateFlow<HomeState>(
        HomeState(
            tasksState = TasksState.Idle,
            articles = Articles(emptyList(), emptyList()),
            workInfo = emptyList()
        )
    )

    init {
        viewModelScope.launch {
            combine(
                tasksRepository.state,
                articleRepository.articles(),
                refreshProvider.workManagerStatus(),
                articleManager.status()
            ) { appState, articles, workManagerStatus, articleManagerStatus ->
                Triple(appState, articles, workManagerStatus to articleManagerStatus)
            }.collectLatest {
                state.value = state.first().copy(
                    tasksState = it.first,
                    articles = Articles(
                        inbox = it.second.filter { !it.longRead },
                        longRead = it.second.filter { it.longRead }
                    ),
                    workInfo = it.third.first + it.third.second.filter { it.state != WorkInfo.State.SUCCEEDED }
                )
            }
        }
        refreshProvider.start()
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

    fun markAsRead(article: Article) {
        viewModelScope.launch {
            articleRepository.markArticleAsRead(article)
        }
    }
}

data class HomeState(val tasksState: TasksState, val articles: Articles, val workInfo: List<WorkInfo>)

data class Articles(val inbox: List<Article>, val longRead: List<Article>)
