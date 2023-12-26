package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalMaterial3Api
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state = viewModel.state.collectAsStateWithLifecycle()
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Notion companion") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DarkGray,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) {
                    HomeScreen(state, it)
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Composable
    private fun HomeScreen(state: State<HomeState>, paddingValues: PaddingValues) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { viewModel.load() }
                ) {
                    Text(text = "Load")
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
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
                        text = "Number of articles: ${state.value.articles.size}"
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
                                text = workInfo.prettyPrint()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun WorkInfo.prettyPrint(): String {
        val date = Date(this.nextScheduleTimeMillis)
        return "WorkInfo [${this.id}]\n" +
                "State: ${this.state}\n" +
                "Next scheduled: ${dateFormatter.format(date)}\n" +
                "Tags: ${this.tags}\n" +
                "${this.constraints}"
    }
}
