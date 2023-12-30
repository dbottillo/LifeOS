package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.notionalert.db.Article
import com.dbottillo.notionalert.feature.articles.ArticleManager
import com.dbottillo.notionalert.notification.NotificationProvider
import com.dbottillo.notionalert.network.RefreshProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val articleManager: ArticleManager
) : ViewModel() {

    val state = MutableStateFlow<HomeState>(
        HomeState(
            appState = AppState.Idle,
            articles = Articles(emptyList(), emptyList()),
            workInfo = emptyList()
        )
    )

    init {
        viewModelScope.launch {
            combine(
                repository.state,
                repository.articles(),
                refreshProvider.workManagerStatus(),
                articleManager.status()
            ) { appState, articles, workManagerStatus, articleManagerStatus ->
                Triple(appState, articles, workManagerStatus to articleManagerStatus)
            }.collectLatest {
                state.value = state.first().copy(
                    appState = it.first,
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
            repository.deleteArticle(article)
        }
    }

    fun markAsRead(article: Article) {
        viewModelScope.launch {
            repository.markArticleAsRead(article)
        }
    }
}

data class HomeState(val appState: AppState, val articles: Articles, val workInfo: List<WorkInfo>)

data class Articles(val inbox: List<Article>, val longRead: List<Article>)
