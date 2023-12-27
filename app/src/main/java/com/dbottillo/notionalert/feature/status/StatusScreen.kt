package com.dbottillo.notionalert.feature.status

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.dbottillo.notionalert.feature.home.AppState
import com.dbottillo.notionalert.feature.home.HomeViewModel
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
                    text = when (val appState = state.value.appState) {
                        is AppState.Idle -> "AppState - Idle"
                        is AppState.Loading -> "AppState - Loading"
                        is AppState.Loaded -> "AppState - Success, last try:\n${appState.timestamp}"
                        is AppState.Error -> "AppState - Error ${appState.message}, last try: ${appState.timestamp}"
                        is AppState.Restored -> "AppState - Restored, last try:\n${appState.timestamp}"
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
        }
    }
}

private fun WorkInfo.prettyPrint(dateFormatter: SimpleDateFormat): String {
    val date = Date(this.nextScheduleTimeMillis)
    return "WorkInfo [${this.id}]\n" +
            "State: ${this.state}\n" +
            "Next scheduled: ${dateFormatter.format(date)}\n" +
            "Tags: ${this.tags}\n" +
            "${this.constraints}"
}
