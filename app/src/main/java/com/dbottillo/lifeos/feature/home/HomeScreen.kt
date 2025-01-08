package com.dbottillo.lifeos.feature.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
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

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        HomeScreenContentExpanded(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            top = state.value.focus,
            blocked = state.value.blocked,
            middle = state.value.projects,
            goals = state.value.goals,
            bottom = state.value.others,
            refresh = viewModel::reloadHome,
            bottomSelection = viewModel::bottomSelection
        )
    } else {
        HomeScreenContent(
            refreshing = state.value.refreshing,
            inbox = state.value.inbox,
            top = state.value.focus,
            blocked = state.value.blocked,
            middle = state.value.projects,
            goals = state.value.goals,
            bottom = state.value.others,
            refresh = viewModel::reloadHome,
            numberOfColumns = if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 3 else 2,
            bottomSelection = viewModel::bottomSelection
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
    refreshing: Boolean,
    inbox: List<EntryContent>,
    top: List<EntryContent>,
    blocked: List<EntryContent>,
    middle: List<EntryContent>,
    goals: List<EntryContent>,
    bottom: HomeStateBottom,
    numberOfColumns: Int,
    refresh: () -> Unit,
    bottomSelection: (BottomSelection) -> Unit
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
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it)
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
                    Entry(content = it)
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
                        Entry(content = it)
                    }
                }
            }
            header {
                Text(
                    text = "Projects",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            middle.forEach {
                item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it)
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
                    Entry(content = it)
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
                                    .clickable {
                                        bottomSelection.invoke(selection.type)
                                    }
                            }
                        )
                    }
                }
            }
            bottom.list.forEach {
                item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it)
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
    top: List<EntryContent>,
    blocked: List<EntryContent>,
    middle: List<EntryContent>,
    goals: List<EntryContent>,
    bottom: HomeStateBottom,
    refresh: () -> Unit,
    bottomSelection: (BottomSelection) -> Unit
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
                            Entry(content = it)
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
                        Entry(content = it)
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
                            Entry(content = it)
                        }
                    }
                }
                header {
                    Text(
                        text = "Projects",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                middle.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it)
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
                        Entry(content = it)
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
                                        .clickable {
                                            bottomSelection.invoke(selection.type)
                                        }
                                }
                            )
                        }
                    }
                }
                bottom.list.forEach {
                    item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                        Entry(content = it)
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
private fun Entry(
    content: EntryContent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.85f
    )
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                context.openLink(content.url)
            },
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
                    numberOfColumns = 2
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
                )
            }
        }
    }
}

private val inbox = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "🔃 Change Phox filter - last: 07/07/2024",
        url = "https://www.phoxwater.com/pages/setup",
        link = "https://www.phoxwater.com/pages/setup",
        subtitle = "24/08",
        parent = "Home Owner",
        color = ColorType.Red.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "NHS app",
        url = "https://www.phoxwater.com/pages/setup",
        color = ColorType.Gray.color
    )
)

private val blocked = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "Map review",
        url = "url",
        color = ColorType.Pink.color
    )
)

private val top = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "Decide tooling",
        url = "url",
        color = ColorType.Blue.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "Replicate home",
        url = "url",
        color = ColorType.Blue.color
    )
)

private val middle = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "Life OS",
        subtitle = "36%",
        url = "",
        color = ColorType.Green.color
    )
)

private val goals = listOf(
    EntryContent(
        id = UUID.randomUUID().toString(),
        title = "Fat 18kg",
        url = "",
        color = ColorType.Aqua.color
    ),
    EntryContent(
        id = UUID.randomUUID().toString(),
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
            title = "Nothing phone testing device",
            subtitle = "12%",
            url = "url",
            color = ColorType.Orange.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            title = "Knowledge hub",
            url = "url",
            link = "https://drive.google.com/file/d/1PYB-TYIXX_w1LkwxQtX0TzG_eLInU9Dv/view?usp=sharing",
            color = ColorType.Yellow.color
        ),
        EntryContent(
            id = UUID.randomUUID().toString(),
            title = "Monitor research",
            url = "url",
            color = ColorType.Purple.color
        )
    )
)
