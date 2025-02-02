package com.dbottillo.lifeos.feature.composer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskComposerActivity : AppCompatActivity() {

    private val viewModel: TaskComposerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.events.consumeEach {
                when(it){
                    ComposerEvents.Finish -> finish()
                }
            }
        }

        viewModel.init(
            url =  intent.getStringExtra(Intent.EXTRA_TEXT),
            title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        )

        setContent {
            val state = viewModel.state.collectAsStateWithLifecycle()
            AppTheme {
                TaskComposerScreen(
                    state = state.value,
                    saveArticle = viewModel::saveArticle,
                    saveLifeOs = viewModel::saveLifeOs
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskComposerScreen(
    state: ComposerState,
    saveArticle: () -> Unit,
    saveLifeOs: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link handler") }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                text = "Url: ${state.url}"
            )
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                text = "Sanitized Url: ${state.sanitizedUrl}"
            )
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                text = "Title: ${state.title}"
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveArticle() },
                    enabled = state.sanitizedUrl != null
                ) {
                    Text(text = "Article")
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveLifeOs() }
                ) {
                    Text(text = "Life Os")
                }
            }
        }
    }
}

@Preview
@Composable
fun ShareScreenPreview() {
    AppTheme {
        TaskComposerScreen(
            state = ComposerState(
                url = "https://www.google.com",
                title = "Google",
            ),
            saveArticle = { },
            saveLifeOs = { },
        )
    }
}
