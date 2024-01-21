@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.dbottillo.lifeos.feature.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.feature.home.HomeViewModel
import com.dbottillo.lifeos.feature.tasks.TasksState
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("UNUSED_PARAMETER", "LongMethod")
@Composable
fun StatusScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    dateFormatter: SimpleDateFormat
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        }
        LazyColumn {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = when (val appState = state.value.tasksState) {
                        is TasksState.Idle -> "AppState - Idle"
                        is TasksState.Loading -> "AppState - Loading"
                        is TasksState.Loaded -> "AppState - Success, last try:\n${appState.timestamp}"
                        is TasksState.Error -> "AppState - Error ${appState.message}, last try: ${appState.timestamp}"
                        is TasksState.Restored -> "AppState - Restored, last try:\n${appState.timestamp}"
                    }
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Number of articles: ${state.value.articles.inbox.size + state.value.articles.longRead.size}"
                )
            }
            if (state.value.workInfo.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = "No work info"
                    )
                }
            } else {
                state.value.workInfo.forEach { workInfo ->
                    item(key = workInfo.id) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            text = workInfo.prettyPrint(dateFormatter)
                        )
                    }
                }
            }
            if (state.value.logs.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = "No logs info"
                    )
                }
            } else {
                state.value.logs.forEach { log ->
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
