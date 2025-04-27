package com.dbottillo.lifeos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
import com.dbottillo.lifeos.ui.EntryContent
import com.dbottillo.lifeos.ui.mapAreas
import com.dbottillo.lifeos.ui.mapFocus
import com.dbottillo.lifeos.ui.mapFolder
import com.dbottillo.lifeos.ui.mapGoals
import com.dbottillo.lifeos.ui.mapIdeas
import com.dbottillo.lifeos.ui.mapInbox
import com.dbottillo.lifeos.ui.mapNextWeek
import com.dbottillo.lifeos.ui.mapResources
import com.dbottillo.lifeos.util.combine
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
            nextWeek = emptyList(),
            folders = emptyList(),
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
            combine(
                tasksRepository.focusFlow,
                tasksRepository.inboxFlow,
                tasksRepository.nextWeekFlow
            ) { focus, inbox, nextWeek ->
                Triple(focus, inbox, nextWeek)
            },
            tasksRepository.foldersFlow,
            tasksRepository.areasFlow,
            tasksRepository.ideasFlow,
            tasksRepository.resourcesFlow,
            otherStateBottomSelection,
            goalsRepository.goalsFlow
        ) { focusInboxNextWeek, folders, areas, ideas, resources, bottomSelection, goals ->
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
                focusInboxNextWeek.second.mapInbox() to focusInboxNextWeek.first.mapFocus(),
                focusInboxNextWeek.third.mapNextWeek() to folders.filter { (it.progress ?: 0f) > 0f }.mapFolder(),
                bottom to goals.filter { it.status is Status.Focus }.mapGoals()
            )
        }.collectLatest { (top, middle, bottom) ->
            homeState.value = homeState.first().copy(
                inbox = top.first,
                focus = top.second,
                nextWeek = middle.first,
                folders = middle.second,
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
    val nextWeek: List<EntryContent>,
    val focus: List<EntryContent>,
    val folders: List<EntryContent>,
    val goals: List<EntryContent>,
    val others: HomeStateBottom,
    val nonBlockingError: Throwable? = null
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

data class Articles(val inbox: List<Article>, val longRead: List<Article>)
