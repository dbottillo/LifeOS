package com.dbottillo.lifeos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
import com.dbottillo.lifeos.ui.EntryContent
import com.dbottillo.lifeos.ui.mapFocus
import com.dbottillo.lifeos.ui.mapFolder
import com.dbottillo.lifeos.ui.mapInbox
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
    private val refreshProvider: RefreshProvider,
    private val widgetsRefresher: WidgetsRefresher,
    private val weeklyTasksUseCase: WeeklyTasksUseCase
) : ViewModel() {

    val homeState = MutableStateFlow(
        HomeState(
            refreshing = false,
            inbox = emptyList(),
            focus = emptyList(),
            folders = emptyList(),
            soon = emptyMap()
        )
    )

    val articleState = MutableStateFlow(
        ArticleScreenState(
            articles = Articles(emptyList(), emptyList()),
        )
    )

    init {
        viewModelScope.launch {
            initHome()
        }
        viewModelScope.launch {
            initArticles()
        }
    }

    @Suppress("LongMethod")
    private suspend fun initHome() {
        combine(
            tasksRepository.focusFlow,
            tasksRepository.inboxFlow,
            tasksRepository.foldersFlow,
            weeklyTasksUseCase.flow
        ) { focus, inbox, folders, soon ->
            Triple(
                inbox.mapInbox(),
                focus.mapFocus(),
                folders.filter { (it.progress ?: 0f) > 0f }.mapFolder() to soon,
            )
        }.collectLatest { (inbox, focus, foldersAndSoon) ->
            homeState.value = homeState.first().copy(
                inbox = inbox,
                focus = focus,
                folders = foldersAndSoon.first,
                soon = foldersAndSoon.second
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

    fun load() {
        refreshProvider.immediate()
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

    fun reloadHome() {
        viewModelScope.launch {
            homeState.value = homeState.first().copy(
                refreshing = true
            )
            val result = tasksRepository.loadNextActions()
            when {
                result.isFailure -> {
                    homeState.value = homeState.first().copy(
                        refreshing = false,
                        nonBlockingError = result.exceptionOrNull()
                    )
                }

                else -> {
                    widgetsRefresher.refreshAll()
                    homeState.value = homeState.first().copy(
                        refreshing = false
                    )
                }
            }
        }
    }

    fun refreshFolders() {
        viewModelScope.launch {
            tasksRepository.loadStaticResources(
                listOf("Folder")
            )
        }
    }

    fun nonBlockingErrorShown() {
        viewModelScope.launch {
            homeState.value = homeState.first().copy(
                nonBlockingError = null
            )
        }
    }
}

data class HomeState(
    val refreshing: Boolean,
    val inbox: List<EntryContent>,
    val focus: List<EntryContent>,
    val folders: List<EntryContent>,
    val soon: Map<String, List<EntryContent>>,
    val nonBlockingError: Throwable? = null
)

data class ArticleScreenState(
    val articles: Articles,
)

data class Articles(val inbox: List<Article>, val longRead: List<Article>)
