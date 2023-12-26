package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.db.Article
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
    private val refreshProvider: RefreshProvider
) : ViewModel() {

    val state = MutableStateFlow<HomeState>(
        HomeState(
        appState = AppState.Idle,
        articles = emptyList()
    )
    )

    init {
        viewModelScope.launch {
            repository.state.combine(repository.articles()) { appState, articles ->
                appState to articles
            }.collectLatest {
                state.value = state.first().copy(
                    appState = it.first,
                    articles = it.second
                )
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            repository.makeNetworkRequest()
            repository.fetchArticles()
            refreshProvider.start()
        }
    }

    fun removeNotification() {
        notificationProvider.clear()
        refreshProvider.stop()
    }
}

data class HomeState(val appState: AppState, val articles: List<Article>)
