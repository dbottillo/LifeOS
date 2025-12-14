package com.dbottillo.lifeos.feature.review

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.dbottillo.lifeos.feature.composer.EXTRA_ENTRY_ID
import com.dbottillo.lifeos.feature.composer.TaskComposerActivity
import com.dbottillo.lifeos.feature.home.Entry
import com.dbottillo.lifeos.feature.home.header
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.ui.ColorType
import com.dbottillo.lifeos.ui.EntryContent
import java.util.UUID

@Composable
fun ReviewScreen(viewModel: ReviewViewModel) {
    val state = viewModel.reviewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    state.value.nonBlockingError?.let { error ->
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        viewModel.nonBlockingErrorShown()
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val openComposer: (String) -> Unit = { entryId ->
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            // navController.navigate(ComposerDialog(entryId = entryId))
        } else {
            val intent = Intent(context, TaskComposerActivity::class.java)
            intent.putExtra(EXTRA_ENTRY_ID, entryId)
            context.startActivity(intent)
        }
    }

    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        ReviewScreenContentExpanded(
            refreshing = state.value.refreshing,
            goals = state.value.goals,
            areas = state.value.areas,
            bottom = state.value.bottom,
            refresh = viewModel::reloadReview,
            openComposer = openComposer,
            bottomSelection = viewModel::bottomSelection,
        )
    } else {
        ReviewScreenContent(
            refreshing = state.value.refreshing,
            goals = state.value.goals,
            areas = state.value.areas,
            bottom = state.value.bottom,
            refresh = viewModel::reloadReview,
            numberOfColumns = if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 3 else 2,
            openComposer = openComposer,
            bottomSelection = viewModel::bottomSelection,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReviewScreenContent(
    refreshing: Boolean,
    goals: List<EntryContent>,
    areas: List<EntryContent>,
    bottom: ReviewStateBottom,
    refresh: () -> Unit,
    openComposer: (String) -> Unit,
    bottomSelection: (ReviewBottomType) -> Unit,
    numberOfColumns: Int
) {
    val pullRefreshState = rememberPullRefreshState(refreshing, refresh)

    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            columns = StaggeredGridCells.Fixed(numberOfColumns),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            header {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            goals.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
            header {
                Text(
                    text = "Areas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            areas.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
            header {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    bottom.selection.forEach { selection ->
                        Text(
                            text = selection.title,
                            style = if (!selection.selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleSmall,
                            color = if (selection.selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                            modifier = if (selection.selected) {
                                Modifier.padding(end = 16.dp)
                            } else {
                                Modifier
                                    .padding(end = 16.dp)
                                    .combinedClickable(
                                        onClick = { bottomSelection.invoke(selection.type) },
                                    )
                            }
                        )
                    }
                }
            }
            bottom.list.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
            header {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }
        }
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReviewScreenContentExpanded(
    refreshing: Boolean,
    goals: List<EntryContent>,
    areas: List<EntryContent>,
    bottom: ReviewStateBottom,
    refresh: () -> Unit,
    openComposer: (String) -> Unit,
    bottomSelection: (ReviewBottomType) -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(refreshing, refresh)

    Box(Modifier.pullRefresh(pullRefreshState)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.weight(0.5f),
                contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                header {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                goals.forEach {
                    item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
                header {
                    Text(
                        text = "Areas",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                areas.forEach {
                    item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
            }
            LazyVerticalStaggeredGrid(
                modifier = Modifier.weight(0.5f),
                contentPadding = PaddingValues(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                header {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        bottom.selection.forEach { selection ->
                            Text(
                                text = selection.title,
                                style = if (!selection.selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleSmall,
                                color = if (selection.selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                modifier = if (selection.selected) {
                                    Modifier.padding(end = 16.dp)
                                } else {
                                    Modifier
                                        .padding(end = 16.dp)
                                        .combinedClickable(
                                            onClick = { bottomSelection.invoke(selection.type) },
                                        )
                                }
                            )
                        }
                    }
                }
                bottom.list.forEach {
                    item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
                header {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }
        }
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

private const val CONTENT_TYPE_ENTRY = "entry"

@Suppress("StringLiteralDuplication")
@Preview(uiMode = UI_MODE_NIGHT_YES, device = "id:pixel_6_pro")
@Composable
fun ReviewScreenPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                ReviewScreenContent(
                    refreshing = false,
                    goals = goals,
                    bottom = bottom,
                    areas = areas,
                    refresh = {},
                    bottomSelection = {},
                    numberOfColumns = 2,
                    openComposer = {}
                )
            }
        }
    }
}

@Suppress("StringLiteralDuplication")
@Preview(uiMode = UI_MODE_NIGHT_YES, device = "id:pixel_tablet")
@Composable
fun ReviewScreenContentExpandedPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                ReviewScreenContentExpanded(
                    refreshing = false,
                    goals = goals,
                    bottom = bottom,
                    areas = areas,
                    refresh = {},
                    bottomSelection = {},
                    openComposer = {}
                )
            }
        }
    }
}

private val goals = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "goal-${UUID.randomUUID()}",
        title = "Fat 18kg",
        url = "",
        color = ColorType.Aqua.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "inbox-${UUID.randomUUID()}",
        title = "Deep on coroutines",
        url = "",
        color = ColorType.Aqua.color
    )
)

private val areas = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "area-${UUID.randomUUID()}",
        title = "Home",
        url = "",
        color = ColorType.Yellow.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "area-${UUID.randomUUID()}",
        title = "Wealth",
        url = "",
        color = ColorType.Yellow.color
    )
)

private val bottom = ReviewStateBottom(
    selection = listOf(
        ReviewBottomSelection(
            title = "Folders (20)",
            selected = true,
            type = ReviewBottomType.FOLDERS
        ),
        ReviewBottomSelection(
            title = "Resources (112)",
            selected = false,
            type = ReviewBottomType.RESOURCES
        )
    ),
    list = listOf(
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "folder-${UUID.randomUUID()}",
            title = "Nothing phone testing device",
            subtitle = "12%",
            url = "url",
            color = ColorType.Green.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "resource-${UUID.randomUUID()}",
            title = "Monitor research",
            url = "url",
            color = ColorType.Purple.color
        )
    )
)
