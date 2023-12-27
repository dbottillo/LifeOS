package com.dbottillo.notionalert.feature.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.notionalert.db.Article
import com.dbottillo.notionalert.feature.home.AppState
import com.dbottillo.notionalert.feature.home.HomeRepository
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
class StatusViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider
) : ViewModel() {

    val state = MutableStateFlow<HomeState>(
        HomeState(
            appState = AppState.Idle,
            articles = emptyList(),
            workInfo = emptyList()
        )
    )

    init {
        viewModelScope.launch {
            combine(
                repository.state,
                repository.articles(),
                refreshProvider.workManagerStatus()
            ) { appState, articles, workManagerStatus ->
                Triple(appState, articles, workManagerStatus)
            }.collectLatest {
                state.value = state.first().copy(
                    appState = it.first,
                    articles = it.second,
                    workInfo = it.third
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
}

data class HomeState(val appState: AppState, val articles: List<Article>, val workInfo: List<WorkInfo>)
