package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterial3Api
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

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
                                containerColor = Color.DarkGray,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(it).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            modifier = Modifier.padding(top = 24.dp),
                            onClick = { viewModel.load() }
                        ) {
                            Text(text = "Load")
                        }
                        Button(
                            modifier = Modifier.padding(top = 24.dp),
                            onClick = { viewModel.removeNotification() }
                        ) {
                            Text(text = "Stop")
                        }
                        Text(
                            modifier = Modifier.padding(top = 32.dp),
                            text = when (val appState = state.value) {
                                is AppState.Idle -> "Idle"
                                is AppState.Loading -> "Loading"
                                is AppState.Loaded -> "Success, last try: ${appState.timestamp}"
                                is AppState.Error -> "Error ${appState.message}, last try: ${appState.timestamp}"
                                is AppState.Restored -> "Restored, last try: ${appState.timestamp}"
                            }
                        )
                    }
                }
            }
        }
    }
}
