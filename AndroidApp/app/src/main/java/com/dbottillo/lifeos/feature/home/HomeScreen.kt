package com.dbottillo.lifeos.feature.home

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
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
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.dbottillo.lifeos.feature.composer.EXTRA_ENTRY_ID
import com.dbottillo.lifeos.feature.composer.TaskComposerActivity
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.ui.ColorType
import com.dbottillo.lifeos.ui.EntryContent
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

private const val CONTENT_TYPE_ENTRY = "entry"
private const val CONTENT_TYPE_TITLE = "title"

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    state.value.nonBlockingError?.let { error ->
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        viewModel.nonBlockingErrorShown()
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val openComposer: (String) -> Unit = { entryId ->
        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            // navController.navigate(ComposerDialog(entryId = entryId))
        } else {
            val intent = Intent(context, TaskComposerActivity::class.java)
            intent.putExtra(EXTRA_ENTRY_ID, entryId)
            context.startActivity(intent)
        }
    }

    if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        HomeScreenContentExpanded(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            focus = state.value.focus,
            folders = state.value.folders,
            soon = state.value.soon,
            refresh = viewModel::reloadHome,
            openComposer = openComposer,
            longPressFolders = viewModel::refreshFolders
        )
    } else {
        HomeScreenContent(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            focus = state.value.focus,
            folders = state.value.folders,
            soon = state.value.soon,
            refresh = viewModel::reloadHome,
            numberOfColumns = if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) 3 else 2,
            openComposer = openComposer,
            refreshFolders = viewModel::refreshFolders,
            paddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContent(
    paddingValues: PaddingValues,
    refreshing: Boolean,
    inbox: List<EntryContent>,
    focus: List<EntryContent>,
    folders: List<EntryContent>,
    soon: Map<String, List<EntryContent>>,
    numberOfColumns: Int,
    refresh: () -> Unit,
    refreshFolders: () -> Unit,
    openComposer: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing, refresh)

    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
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
            focus.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
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
            folders.forEach {
                item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it, openComposer = openComposer)
                }
            }
            if (soon.isNotEmpty()) {
                header {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    )
                }
                soon.entries.forEach { (weekStart, items) ->
                    header {
                        Text(
                            text = if (weekStart == "No date") "No date" else weekStart,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 8.dp)
                        )
                    }
                    items.forEach {
                        item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                            Entry(content = it, openComposer = openComposer)
                        }
                    }
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContentExpanded(
    refreshing: Boolean,
    inbox: List<EntryContent>,
    focus: List<EntryContent>,
    folders: List<EntryContent>,
    soon: Map<String, List<EntryContent>>,
    refresh: () -> Unit,
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
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
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
                focus.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it, openComposer = openComposer)
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
                folders.forEach {
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
            LazyVerticalStaggeredGrid(
                modifier = Modifier.weight(0.5f),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (soon.isNotEmpty()) {
                    soon.entries.forEach { (weekStart, items) ->
                        header {
                            Text(
                                text = if (weekStart == "No date") "No date" else weekStart,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 8.dp)
                            )
                        }
                        items.forEach {
                            item(key = it.displayId, contentType = CONTENT_TYPE_ENTRY) {
                                Entry(content = it, openComposer = openComposer)
                            }
                        }
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
                    focus = focus,
                    folders = folders,
                    soon = soon,
                    refresh = {},
                    numberOfColumns = 2,
                    refreshFolders = {},
                    openComposer = {},
                    paddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
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
                    focus = focus,
                    folders = folders,
                    soon = soon,
                    refresh = {},
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

private val focus = listOf(
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

private val folders = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        displayId = "folder-${UUID.randomUUID()}",
        title = "Life OS",
        subtitle = "36%",
        url = "",
        color = ColorType.Green.color
    )
)

private val soon = mapOf(
    "28/04" to listOf(
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "soon-${UUID.randomUUID()}",
            title = "Testing",
            url = "url",
            color = ColorType.Blue.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "soon-${UUID.randomUUID()}",
            title = "Call GP",
            url = "url",
            color = ColorType.Blue.color
        )
    ),
    "05/05" to listOf(
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "soon-${UUID.randomUUID()}",
            title = "Check application",
            url = "url",
            color = ColorType.Blue.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            displayId = "soon-${UUID.randomUUID()}",
            title = "Book Museum",
            url = "url",
            color = ColorType.Blue.color
        )
    )
)
