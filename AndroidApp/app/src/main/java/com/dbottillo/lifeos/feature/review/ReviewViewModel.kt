package com.dbottillo.lifeos.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.ui.EntryContent
import com.dbottillo.lifeos.ui.mapAreas
import com.dbottillo.lifeos.ui.mapGoals
import com.dbottillo.lifeos.ui.mapIdeas
import com.dbottillo.lifeos.ui.mapResources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val goalsRepository: GoalsRepository,
    private val widgetsRefresher: WidgetsRefresher
) : ViewModel() {

    private val otherStateBottomSelection = MutableStateFlow(
        ReviewBottomType.IDEAS
    )

    val reviewState = MutableStateFlow(
        ReviewState(
            refreshing = false,
            areas = emptyList(),
            goals = emptyList(),
            bottom = ReviewStateBottom(
                selection = listOf(),
                list = emptyList()
            ),
        )
    )

    init {
        viewModelScope.launch {
            initHome()
        }
    }

    @Suppress("LongMethod")
    private suspend fun initHome() {
        combine(
            tasksRepository.areasFlow,
            tasksRepository.ideasFlow,
            tasksRepository.resourcesFlow,
            otherStateBottomSelection,
            goalsRepository.goalsFlow
        ) { areas, ideas, resources, bottomSelection, goals ->
            val uiAreas = areas.mapAreas()
            val uiResources = resources.mapResources()
            val uiIdeas = ideas.mapIdeas()
            val bottom = ReviewStateBottom(
                selection = listOf(
                    ReviewBottomSelection(
                        title = "Ideas (${uiIdeas.size})",
                        selected = bottomSelection == ReviewBottomType.IDEAS,
                        type = ReviewBottomType.IDEAS
                    ),
                    ReviewBottomSelection(
                        title = "Resources (${uiResources.size})",
                        selected = bottomSelection == ReviewBottomType.RESOURCES,
                        type = ReviewBottomType.RESOURCES
                    )
                ),
                list = when (bottomSelection) {
                    ReviewBottomType.RESOURCES -> uiResources
                    ReviewBottomType.IDEAS -> uiIdeas
                }
            )
            Triple(
                goals.filter { it.status is Status.Focus }.mapGoals(),
                uiAreas,
                bottom
            )
        }.collectLatest { (top, middle, bottom) ->
            reviewState.value = reviewState.first().copy(
                goals = top,
                areas = middle,
                bottom = bottom,
            )
        }
    }

    fun reloadReview() {
        viewModelScope.launch {
            reviewState.value = reviewState.first().copy(
                refreshing = true
            )
            val result = tasksRepository.loadStaticResources(
                listOf("Folder", "Area", "Goal", "Idea", "Resource")
            )
            when {
                result.isFailure -> {
                    reviewState.value = reviewState.first().copy(
                        refreshing = false,
                        nonBlockingError = result.exceptionOrNull()
                    )
                }
                else -> {
                    widgetsRefresher.refreshAll()
                    reviewState.value = reviewState.first().copy(
                        refreshing = false
                    )
                }
            }
        }
    }

    fun bottomSelection(type: ReviewBottomType) {
        otherStateBottomSelection.value = type
    }

    fun nonBlockingErrorShown() {
        viewModelScope.launch {
            reviewState.value = reviewState.first().copy(
                nonBlockingError = null
            )
        }
    }
}

data class ReviewState(
    val refreshing: Boolean,
    val goals: List<EntryContent>,
    val areas: List<EntryContent>,
    val bottom: ReviewStateBottom,
    val nonBlockingError: Throwable? = null
)

data class ReviewStateBottom(
    val selection: List<ReviewBottomSelection>,
    val list: List<EntryContent>
)

data class ReviewBottomSelection(
    val title: String,
    val selected: Boolean,
    val type: ReviewBottomType
)

enum class ReviewBottomType {
    IDEAS,
    RESOURCES
}
