package com.dbottillo.lifeos.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dbottillo.lifeos.feature.tasks.NextAction
import com.dbottillo.lifeos.ui.AppTheme

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.homeState.collectAsStateWithLifecycle()
    HomeScreenContent(
        refreshing = state.value.refreshing,
        nextAction = state.value.nextActions,
        refresh = viewModel::reloadHome
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreenContent(
    refreshing: Boolean,
    nextAction: List<NextAction>,
    refresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing, refresh)
    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            nextAction.forEach {
                item(key = it.url) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(it.text)
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
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
                            text = "Decide tooling",
                            color = "blue",
                            due = "",
                            url = "url"
                        ),
                        NextAction(
                            text = "Replicate home",
                            color = "blue",
                            due = "",
                            url = "url"
                        ),
                        NextAction(
                            text = "AOC '23 Day 09 Test Long one",
                            color = "blue",
                            due = "",
                            url = "url"
                        )
                    ),
                    refresh = {}
                )
            }
        }
    }
}
