package com.dbottillo.lifeos.feature.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

const val CONTENT_TYPE_ENTRY = "entry"
const val CONTENT_TYPE_TITLE = "title"

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()

    state.value.nonBlockingError?.let { error ->
        Toast.makeText(LocalContext.current, error.message, Toast.LENGTH_SHORT).show()
        viewModel.nonBlockingErrorShown()
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val openComposer: (String) -> Unit = { entryId ->
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            navController.navigate(ComposerDialog(entryId = entryId))
        } else {
            navController.navigate(Composer(entryId = entryId)) {
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        HomeScreenContentExpanded(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            top = state.value.focus,
            blocked = state.value.blocked,
            middle = state.value.folders,
            goals = state.value.goals,
            bottom = state.value.others,
            refresh = viewModel::reloadHome,
            openComposer = openComposer,
            bottomSelection = viewModel::bottomSelection,
            bottomSelectionDoubleTap = viewModel::bottomSelectionDoubleTap,
            longPressFolders = viewModel::refreshFolders
        )
    } else {
        HomeScreenContent(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            top = state.value.focus,
            blocked = state.value.blocked,
            middle = state.value.folders,
            goals = state.value.goals,
            bottom = state.value.others,
            refresh = viewModel::reloadHome,
            numberOfColumns = if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 3 else 2,
            openComposer = openComposer,
            bottomSelection = viewModel::bottomSelection,
            bottomSelectionDoubleTap = viewModel::bottomSelectionDoubleTap,
            refreshFolders = viewModel::refreshFolders
        )
    }
}

fun LazyStaggeredGridScope.header(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit
) {
    item(
        span = StaggeredGridItemSpan.FullLine,
        content = content,
        contentType = CONTENT_TYPE_TITLE
    )
}

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    refreshing: Boolean,
    inbox: List<EntryContent>,
    top: List<EntryContent>,
    blocked: List<EntryContent>,
    middle: List<EntryContent>,
    goals: List<EntryContent>,
    bottom: HomeStateBottom,
    numberOfColumns: Int,
    refresh: () -> Unit,
    bottomSelection: (BottomSelection) -> Unit,
    bottomSelectionDoubleTap: (BottomSelection) -> Unit,
    refreshFolders: () -> Unit,
    openComposer: (String) -> Unit
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
            if (inbox.isNotEmpty()) {
                header {
                    Text(
                        text = "Inbox",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                inbox.forEach {
                    item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
            }
            header {
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (inbox.isEmpty()) 0.dp else 16.dp)
                )
            }
            top.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
            if (blocked.isNotEmpty()) {
                header {
                    Text(
                        text = "Blocked",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                blocked.forEach {
                    item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
            }
            header {
                Text(
                    text = "Open Folders",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .combinedClickable(
                            onClick = { },
                            onDoubleClick = {
                                refreshFolders.invoke()
                            },
                        )
                )
            }
            middle.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
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
                                        onDoubleClick = {
                                            bottomSelectionDoubleTap.invoke(selection.type)
                                        },
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

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContentExpanded(
    refreshing: Boolean,
    inbox: List<EntryContent>,
    top: List<EntryContent>,
    blocked: List<EntryContent>,
    middle: List<EntryContent>,
    goals: List<EntryContent>,
    bottom: HomeStateBottom,
    refresh: () -> Unit,
    bottomSelection: (BottomSelection) -> Unit,
    bottomSelectionDoubleTap: (BottomSelection) -> Unit,
    longPressFolders: () -> Unit,
    openComposer: (String) -> Unit
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
                if (inbox.isNotEmpty()) {
                    header {
                        Text(
                            text = "Inbox",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    inbox.forEach {
                        item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                            Entry(content = it, openComposer = openComposer)
                        }
                    }
                }
                header {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (inbox.isEmpty()) 0.dp else 16.dp)
                    )
                }
                top.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
                if (blocked.isNotEmpty()) {
                    header {
                        Text(
                            text = "Blocked",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                    blocked.forEach {
                        item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                            Entry(content = it, openComposer = openComposer)
                        }
                    }
                }
                header {
                    Text(
                        text = "Folders",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    longPressFolders.invoke()
                                },
                            )
                    )
                }
                middle.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
                    }
                }
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
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
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
                                            onDoubleClick = {
                                                bottomSelectionDoubleTap.invoke(selection.type)
                                            },
                                        )
                                }
                            )
                        }
                    }
                }
                bottom.list.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Entry(
    content: EntryContent,
    modifier: Modifier = Modifier,
    openComposer: (String) -> Unit
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.85f
    )
    Surface(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    context.openLink(content.url)
                },
                onLongClick = {
                    openComposer.invoke(content.id)
                },
            ),
        color = content.color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 20.sp,
                ),
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (content.subtitle?.isNotEmpty() == true) {
                Text(
                    text = content.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp),
                    color = subtitleColor,
                )
            }
            if (content.link?.isNotEmpty() == true) {
                Text(
                    text = content.link,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable {
                            context.openLink(content.link)
                        }
                )
            }
            if (content.parent?.isNotEmpty() == true) {
                Text(
                    text = content.parent,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

@Suppress("StringLiteralDuplication")
@Preview(uiMode = UI_MODE_NIGHT_YES, device = "id:pixel_6_pro")
@Composable
fun HomeScreenPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                HomeScreenContent(
                    refreshing = false,
                    inbox = inbox,
                    blocked = blocked,
                    top = top,
                    middle = middle,
                    bottom = bottom,
                    goals = goals,
                    refresh = {},
                    bottomSelection = {},
                    bottomSelectionDoubleTap = {},
                    numberOfColumns = 2,
                    refreshFolders = {},
                    openComposer = {}
                )
            }
        }
    }
}

@Suppress("StringLiteralDuplication")
@Preview(uiMode = UI_MODE_NIGHT_YES, device = "id:pixel_tablet")
@Composable
fun HomeScreenContentExpandedPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                HomeScreenContentExpanded(
                    refreshing = false,
                    inbox = inbox,
                    blocked = blocked,
                    top = top,
                    middle = middle,
                    bottom = bottom,
                    goals = goals,
                    refresh = {},
                    bottomSelection = {},
                    bottomSelectionDoubleTap = {},
                    longPressFolders = {},
                    openComposer = {}
                )
            }
        }
    }
}

private val inbox = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "inbox-${UUID.randomUUID()}",
        title = "🔃 Change Phox filter - last: 07/07/2024",
        url = "https://www.phoxwater.com/pages/setup",
        link = "https://www.phoxwater.com/pages/setup",
        subtitle = "24/08",
        parent = "Home Owner",
        color = ColorType.Red.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "inbox-${UUID.randomUUID()}",
        title = "NHS app",
        url = "https://www.phoxwater.com/pages/setup",
        color = ColorType.Gray.color
    )
)

private val blocked = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "blocked-${UUID.randomUUID()}",
        title = "Map review",
        url = "url",
        color = ColorType.Pink.color
    )
)

private val top = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "focus-${UUID.randomUUID()}",
        title = "Decide tooling",
        url = "url",
        color = ColorType.Blue.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "focus-${UUID.randomUUID()}",
        title = "Replicate home",
        url = "url",
        color = ColorType.Blue.color
    )
)

private val middle = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "folder-${UUID.randomUUID()}",
        title = "Life OS",
        subtitle = "36%",
        url = "",
        color = ColorType.Green.color
    )
)

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

private val bottom = HomeStateBottom(
    selection = listOf(
        HomeBottomSelection(
            title = "Ideas (100)",
            selected = true,
            type = BottomSelection.IDEAS
        ),
        HomeBottomSelection(
            title = "Areas (15)",
            selected = false,
            type = BottomSelection.AREAS
        ),
        HomeBottomSelection(
            title = "Resources (112)",
            selected = false,
            type = BottomSelection.RESOURCES
        )
    ),
    list = listOf(
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "idea-${UUID.randomUUID()}",
            title = "Nothing phone testing device",
            subtitle = "12%",
            url = "url",
            color = ColorType.Orange.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "area-${UUID.randomUUID()}",
            title = "Knowledge hub",
            url = "url",
            link = "https://drive.google.com/file/d/1PYB-TYIXX_w1LkwxQtX0TzG_eLInU9Dv/view?usp=sharing",
            color = ColorType.Yellow.color
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
