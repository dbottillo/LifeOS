package com.dbottillo.lifeos.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dbottillo.lifeos.db.BlockParagraph
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

const val CONTENT_TYPE_ENTRY = "entry"
const val CONTENT_TYPE_PARAGRAPH = "paragraph"
const val CONTENT_TYPE_TITLE = "title"

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()
    HomeScreenContent(
        refreshing = state.value.refreshing,
        inbox = state.value.inbox,
        top = state.value.focus,
        middle = state.value.projects,
        bottom = state.value.others,
        goals = state.value.goals,
        refresh = viewModel::reloadHome,
        bottomSelection = viewModel::bottomSelection
    )
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

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContent(
    refreshing: Boolean,
    inbox: List<EntryContent>,
    top: List<EntryContent>,
    middle: List<EntryContent>,
    bottom: HomeStateBottom,
    goals: List<BlockParagraph>,
    refresh: () -> Unit,
    bottomSelection: (BottomSelection) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing, refresh)
    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = if (inbox.isEmpty()) 0.dp else 16.dp)
                )
            }
            top.forEach {
                item(key = it.id, contentType = CONTENT_TYPE_ENTRY) {
                    Entry(content = it)
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
                Column {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp)
                    )
                    Goals(goals)
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

@Composable
private fun Entry(
    content: EntryContent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                context.openLink(content.url)
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(text = content.title, style = MaterialTheme.typography.bodyLarge)
            if (content.subtitle?.isNotEmpty() == true) {
                Text(
                    text = content.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (content.link?.isNotEmpty() == true) {
                Text(
                    text = content.link,
                    style = MaterialTheme.typography.bodySmall,
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
                    modifier = Modifier
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.Goals(
    goals: List<BlockParagraph>
) {
   goals.forEach { paragraph ->
       val text = if (paragraph.type == "numbered_list_item") {
           "${paragraph.index}. ${paragraph.text}"
       } else {
           paragraph.text
       }
       Text(text = text, modifier = Modifier.padding(top = 2.dp))
   }
}

@Suppress("StringLiteralDuplication")
@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                HomeScreenContent(
                    refreshing = false,
                    inbox = listOf(
                        EntryContent(
                            id = UUID.randomUUID().toString(),
                            title = "Do grocery",
                            url = "url",
                        )
                    ),
                    top = listOf(
                        EntryContent(
                            id = UUID.randomUUID().toString(),
                            title = "Decide tooling",
                            url = "url",
                        ),
                        EntryContent(
                            id = UUID.randomUUID().toString(),
                            title = "Replicate home",
                            url = "url"
                        )
                    ),
                    middle = emptyList(),
                    bottom = HomeStateBottom(
                        selection = emptyList(),
                        list = emptyList()
                    ),
                    goals = emptyList(),
                    refresh = {},
                    bottomSelection = {}
                )
            }
        }
    }
}
