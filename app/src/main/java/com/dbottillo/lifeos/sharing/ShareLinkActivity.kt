package com.dbottillo.lifeos.sharing

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareLinkActivity : AppCompatActivity() {

    private val viewModel: SharingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.events.consumeEach {
                if (it) finish()
            }
        }

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (intent.action.equals(Intent.ACTION_SEND) && url != null) {
            val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            setContent {
                AppTheme {
                    ShareLinkScreen(
                        url = url,
                        title = title,
                        saveArticle = viewModel::saveArticle,
                        saveLifeOs = viewModel::saveLifeOs
                    )
                }
            }
        } else {
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLinkScreen(
    url: String,
    title: String?,
    saveArticle: (String, String?) -> Unit,
    saveLifeOs: (String, String?) -> Unit
) {
    val sanitizedUrl = remember { url.split("?").first() }
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
                text = "Url: $url"
            )
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                text = "Sanitized Url: $sanitizedUrl"
            )
            Text(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                text = "Title: $title"
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
                    onClick = { saveArticle(sanitizedUrl, title) }
                ) {
                    Text(text = "Article")
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveLifeOs(sanitizedUrl, title) }
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
        ShareLinkScreen(
            url = "https://www.google.com",
            title = "Google",
            saveArticle = { _, _ -> },
            saveLifeOs = { _, _ -> },
        )
    }
}
