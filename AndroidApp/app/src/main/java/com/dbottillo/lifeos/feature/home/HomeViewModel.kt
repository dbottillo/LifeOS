package com.dbottillo.lifeos.feature.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.NextWeek
import com.dbottillo.lifeos.feature.tasks.Focus
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Idea
import com.dbottillo.lifeos.feature.tasks.Inbox
import com.dbottillo.lifeos.feature.tasks.Folder
import com.dbottillo.lifeos.feature.tasks.Resource
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
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

data class EntryContent(
    val id: String,
    val displayId: String,
    val title: String,
    val url: String,
    val subtitle: String? = null,
    val link: String? = null,
    val parent: String? = null,
    val color: Color
)

fun List<Inbox>.mapInbox(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "inbox-${it.id}",
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Focus>.mapFocus(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "focus-${it.id}",
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Folder>.mapFolder(): List<EntryContent> {
    return map {
        val subtitle = it.progress?.let { "${(it * 100).toInt()}%" } ?: ""
        EntryContent(
            id = it.id,
            displayId = "folder-${it.id}",
            title = it.text,
            subtitle = subtitle,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Green.color
        )
    }
}

fun List<NextWeek>.mapNextWeek(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "next-week-${it.id}",
            title = it.text,
            subtitle = it.dueFormatted,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Orange.color
        )
    }
}

fun List<Area>.mapAreas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "area-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            color = ColorType.Yellow.color
        )
    }
}

fun List<Idea>.mapIdeas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "idea-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Orange.color
        )
    }
}

fun List<Resource>.mapResources(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "resource-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Purple.color
        )
    }
}

fun List<Goal>.mapGoals(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "goal-${it.id}",
            title = it.text,
            url = it.url,
            parent = it.parent?.title,
            color = ColorType.Aqua.color
        )
    }
}

fun String?.toColor(): Color {
    return when (this) {
        "gray" -> ColorType.Gray.color
        "orange" -> ColorType.Orange.color
        "green" -> ColorType.Green.color
        "blue" -> ColorType.Blue.color
        "red" -> ColorType.Red.color
        "purple" -> ColorType.Purple.color
        "pink" -> ColorType.Pink.color
        "yellow" -> ColorType.Yellow.color
        else -> ColorType.Gray.color
    }
}

@Suppress("MagicNumber")
enum class ColorType(val color: Color) {
    Gray(Color(0xFF777777)),
    Orange(Color(0xFF8D4A13)),
    Green(Color(0xFF2C6845)),
    Blue(Color(0xFF183A69)),
    Red(Color(0xFF751E1E)),
    Purple(Color(0xFF634681)),
    Pink(Color(0xFF8B1E77)),
    Yellow(Color(0xFF684511)),
    Aqua(Color(0xFF00535D)),
}
