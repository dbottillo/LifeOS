package com.dbottillo.lifeos.feature.status

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Log
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun StatusScreen(
    viewModel: StatusViewModel,
    dateFormatter: SimpleDateFormat
) {
    StatusScreenContent(viewModel = viewModel, dateFormatter = dateFormatter)
}

@Suppress("UNUSED_PARAMETER", "LongMethod")
@Composable
fun StatusScreenContent(
    viewModel: StatusViewModel,
    dateFormatter: SimpleDateFormat
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    state.value.nonBlockingError?.let { error ->
        Toast.makeText(LocalContext.current, error.message, Toast.LENGTH_SHORT).show()
        viewModel.nonBlockingErrorShown()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState(
            pageCount = { pages.size }
        )
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { pageIndex ->
            when (pages[pageIndex]) {
                Page.Logs -> LogList(state.value.logs, dateFormatter)
                Page.Daily -> WorkManagerList("Daily", state.value.daily, dateFormatter)
                Page.Periodic -> WorkManagerList("Periodic", state.value.periodic, dateFormatter)
                Page.Articles -> WorkManagerList("Articles", state.value.articles, dateFormatter)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.load() }
            ) {
                Text(text = "Load")
            }
            Button(
                onClick = { viewModel.stop() }
            ) {
                Text(text = "Stop")
            }
            if (state.value.allLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(48.dp).align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                Button(
                    onClick = { viewModel.reloadAll() }
                ) {
                    Text(text = "All")
                }
            }
            Button(
                onClick = { viewModel.refreshWidget() }
            ) {
                Text(text = "Widget")
            }
        }
    }
}

@Composable
private fun LogList(logs: List<Log>, dateFormatter: SimpleDateFormat) {
    LazyColumn {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Logs",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (logs.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "No logs info"
                )
            }
        } else {
            logs.forEach { log ->
                item(key = log.id) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = log.prettyPrint(dateFormatter),
                        color = when (log.level) {
                            "info" -> Color.White
                            "debug" -> Color.Yellow
                            "error" -> Color.Red
                            else -> Color.Black
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkManagerList(
    type: String,
    workInfo: List<WorkInfo>,
    dateFormatter: SimpleDateFormat
) {
    LazyColumn {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Work Info - $type",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (workInfo.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "No work info"
                )
            }
        } else {
            workInfo.forEach { workInfo ->
                item(key = workInfo.id) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = workInfo.prettyPrint(dateFormatter)
                    )
                }
            }
        }
    }
}

private fun WorkInfo.prettyPrint(dateFormatter: SimpleDateFormat): String {
    this.stopReason
    val date = Date(this.nextScheduleTimeMillis)
    return "WorkInfo [${this.id}]\n" +
            "State: ${this.state}\n" +
            "Next scheduled: ${dateFormatter.format(date)}\n" +
            "Tags: ${this.tags}\n" +
            "Run attempt count: ${this.runAttemptCount}"
}

private fun Log.prettyPrint(dateFormatter: SimpleDateFormat): String {
    return "Log [${this.id}]\n" +
            "Tag: ${this.tag}\n" +
            "Level: ${this.level}\n" +
            "Message: ${this.message}\n" +
            "Created at: ${dateFormatter.format(Date(this.createdAt))}"
}

sealed class Page {
    data object Logs : Page()
    data object Daily : Page()
    data object Periodic : Page()
    data object Articles : Page()
}

val pages = listOf(
    Page.Logs,
    Page.Periodic,
    Page.Daily,
    Page.Articles
)
