package com.dbottillo.lifeos.feature.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
import com.dbottillo.lifeos.notification.NotificationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val articleManager: ArticleManager,
    private val logsRepository: LogsRepository,
    private val widgetsRefresher: WidgetsRefresher
) : ViewModel() {

    init {
        viewModelScope.launch {
            initStatus()
        }
    }

    val state = MutableStateFlow(
        StatusScreenState(
            daily = emptyList(),
            periodic = emptyList(),
            articles = emptyList(),
            logs = emptyList(),
            allLoading = false
        )
    )

    private suspend fun initStatus() {
        combine(
            refreshProvider.periodicStatus(),
            refreshProvider.dailyStatus(),
            articleManager.status(),
            logsRepository.entries()
        ) { periodic, daily, articles, logs ->
            Pair(logs to daily, periodic to articles)
        }.collectLatest {
            state.value = state.first().copy(
                daily = it.first.second,
                periodic = it.second.first,
                articles = it.second.second,
                logs = it.first.first
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

    fun reloadAll() {
        viewModelScope.launch {
            state.value = state.first().copy(
                allLoading = true
            )
            val result = tasksRepository.loadStaticResources(
                listOf("Project", "Area", "Goal", "Idea", "Resource")
            )
            when {
                result.isFailure -> {
                    state.value = state.first().copy(
                        allLoading = false,
                        nonBlockingError = result.exceptionOrNull()
                    )
                }
                else -> {
                    widgetsRefresher.refreshAll()
                    state.value = state.first().copy(
                        allLoading = false
                    )
                }
            }
        }
    }

    fun refreshWidget() {
        widgetsRefresher.refreshAll()
    }

    fun nonBlockingErrorShown() {
        viewModelScope.launch {
            state.value = state.first().copy(
                nonBlockingError = null
            )
        }
    }
}

data class StatusScreenState(
    val daily: List<WorkInfo>,
    val periodic: List<WorkInfo>,
    val articles: List<WorkInfo>,
    val logs: List<Log>,
    val allLoading: Boolean,
    val nonBlockingError: Throwable? = null
)
