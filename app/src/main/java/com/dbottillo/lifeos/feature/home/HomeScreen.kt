package com.dbottillo.lifeos.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.NextAction
import com.dbottillo.lifeos.feature.tasks.Project
import com.dbottillo.lifeos.feature.tasks.Status
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()
    HomeScreenContent(
        refreshing = state.value.refreshing,
        nextAction = state.value.nextActions,
        projects = state.value.projects,
        areas = state.value.areas,
        refresh = viewModel::reloadHome
    )
}

fun LazyStaggeredGridScope.header(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit
) {
    item(span = StaggeredGridItemSpan.FullLine, content = content)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContent(
    refreshing: Boolean,
    nextAction: List<NextAction>,
    projects: List<Project>,
    areas: List<Area>,
    refresh: () -> Unit
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
            header {
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            nextAction.forEach { action ->
                item(key = action.id) {
                    Entry(
                        content = EntryContent(
                            title = action.text,
                            subtitle = action.due,
                            url = action.url
                        )
                    )
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
            projects.forEach { project ->
                item(key = project.id) {
                    val subtitle = project.progress?.let { "${(it * 100).toInt()}%" } ?: ""
                    Entry(
                        content = EntryContent(
                            title = project.text,
                            subtitle = subtitle,
                            url = project.url
                        )
                    )
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
            areas.forEach { area ->
                item(key = area.id) {
                    Entry(
                        content = EntryContent(
                            title = area.text,
                            url = area.url
                        )
                    )
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

data class EntryContent(val title: String, val url: String, val subtitle: String? = null)

@Composable
private fun Entry(
    content: EntryContent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxSize().clickable {
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
        }
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
                    nextAction = listOf(
                        NextAction(
                            id = UUID.randomUUID().toString(),
                            text = "Decide tooling",
                            color = "blue",
                            due = "",
                            url = "url"
                        ),
                        NextAction(
                            id = UUID.randomUUID().toString(),
                            text = "Replicate home",
                            color = "blue",
                            due = "",
                            url = "url"
                        ),
                        NextAction(
                            id = UUID.randomUUID().toString(),
                            text = "AOC '23 Day 09 Test Long one",
                            color = "blue",
                            due = "",
                            url = "url"
                        )
                    ),
                    projects = listOf(
                        Project(
                            id = UUID.randomUUID().toString(),
                            text = "Decide tooling",
                            color = "blue",
                            due = "",
                            url = "url",
                            progress = null,
                            status = Status.Focus
                        ),
                        Project(
                            id = UUID.randomUUID().toString(),
                            text = "Replicate home",
                            color = "blue",
                            due = "",
                            url = "url",
                            progress = 0.2f,
                            status = Status.Focus
                        ),
                        Project(
                            id = UUID.randomUUID().toString(),
                            text = "AOC '23 Day 09 Test Long one",
                            color = "blue",
                            due = "",
                            url = "url",
                            progress = 1.0f,
                            status = Status.Focus
                        )
                    ),
                    areas = emptyList(),
                    refresh = {}
                )
            }
        }
    }
}
