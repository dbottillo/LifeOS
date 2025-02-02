package com.dbottillo.lifeos.feature.composer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

        enableEdgeToEdge()

        lifecycleScope.launch {
            viewModel.events.consumeEach {
                when (it) {
                    ComposerEvents.Finish -> finish()
                }
            }
        }

        viewModel.init(
            url = intent.getStringExtra(Intent.EXTRA_TEXT),
            title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        )

        setContent {
            val state = viewModel.state.collectAsStateWithLifecycle()
            AppTheme {
                TaskComposerScreen(
                    state = state.value,
                    onTitleChange = viewModel::onTitleChange,
                    onUrlChange = viewModel::onUrlChange,
                    saveArticle = viewModel::saveArticle,
                    saveLifeOs = viewModel::saveLifeOs
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskComposerScreen(
    state: ComposerState,
    onTitleChange: (String) -> Unit = {},
    onUrlChange: (String) -> Unit = {},
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
        Column(
            modifier = Modifier
                .consumeWindowInsets(it)
                .padding(it)
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") }
                )
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.sanitizedUrl,
                        onValueChange = onUrlChange,
                        label = { Text("Url") }
                    )
                    if (state.url != state.sanitizedUrl) {
                        Text(
                            text = "Original url: ${state.url}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveLifeOs() }
                ) {
                    Text(text = "Task")
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveArticle() },
                    enabled = state.validUrl
                ) {
                    Text(text = "Article")
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
                url = "https://www.google.com?abc=def",
                title = "Google",
            ),
            saveArticle = { },
            saveLifeOs = { },
        )
    }
}
