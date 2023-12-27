package com.dbottillo.notionalert.feature.articles

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dbottillo.notionalert.db.Article
import com.dbottillo.notionalert.feature.home.HomeViewModel

@Suppress("UNUSED_PARAMETER")
@Composable
fun ArticlesScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.value.articles.inbox.isNotEmpty()) {
            item(key = "inbox") {
                Section("Inbox (${state.value.articles.inbox.size})",)
            }
            state.value.articles.inbox.forEach { article ->
                item(key = article.uid) {
                    Article(article)
                }
            }
        }
        if (state.value.articles.longRead.isNotEmpty()) {
            item(key = "long read") {
                Section("Long read (${state.value.articles.longRead.size})")
            }
            state.value.articles.longRead.forEach { article ->
                item(key = article.uid) {
                    Article(article)
                }
            }
        }
    }
}

@Composable
fun Section(text: String) {
    Text(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun Article(article: Article) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
        .padding(horizontal = 8.dp)
        .fillMaxWidth()
        .defaultMinSize(minHeight = 48.dp)
        .clickable {
            val intentUrl = Intent(Intent.ACTION_VIEW)
            intentUrl.data = Uri.parse(article.url)
            intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentUrl)
        }
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                text = article.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                text = article.url,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
