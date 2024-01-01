package com.dbottillo.lifeos.feature.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.home.HomeViewModel
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

@Suppress("UNUSED_PARAMETER")
@Composable
fun ArticlesScreen(navController: NavController, viewModel: HomeViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    ArticlesScreenContent(
        inbox = state.value.articles.inbox,
        longRead = state.value.articles.longRead,
        markAsRead = viewModel::markAsRead,
        delete = viewModel::delete
    )
}

@Composable
fun ArticlesScreenContent(
    inbox: List<Article>,
    longRead: List<Article>,
    markAsRead: (Article) -> Unit,
    delete: (Article) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (inbox.isNotEmpty()) {
            item(key = "inbox") {
                Section("Inbox (${inbox.size})",)
            }
            inbox.forEachIndexed { index, article ->
                item(key = article.uid) {
                    Article(article, index, markAsRead, delete)
                }
            }
        }
        if (longRead.isNotEmpty()) {
            item(key = "long read") {
                Section("Long read (${longRead.size})")
            }
            inbox.forEachIndexed { index, article ->
                item(key = article.uid) {
                    Article(article, index, markAsRead, delete)
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
fun Article(
    article: Article,
    index: Int,
    markAsRead: (Article) -> Unit,
    delete: (Article) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable {
                context.openLink(article.url)
            }
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                text = "${index + 1}. ${article.title}",
                style = MaterialTheme.typography.titleMedium.copy(
                    lineHeight = 20.sp
                )
            )
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp),
                text = article.url,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { markAsRead.invoke(article) }) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.baseline_checkmark),
                        contentDescription = "mark as read"
                    )
                }
                IconButton(onClick = { delete.invoke(article) }) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.baseline_bin),
                        contentDescription = "mark as read"
                    )
                }
            }
        }
    }
}

@Suppress("StringLiteralDuplication")
@Preview
@Composable
fun ArticlesScreenPreview() {
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                ArticlesScreenContent(
                    inbox = listOf(
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title",
                            url = "article url",
                            longRead = false,
                            status = "synced"
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 2",
                            url = "article url 2",
                            longRead = false,
                            status = "synced"
                        )
                    ),
                    longRead = listOf(
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 3",
                            url = "article url",
                            longRead = false,
                            status = "synced"
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 4",
                            url = "article url 2",
                            longRead = false,
                            status = "synced"
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 5",
                            url = "article url 2",
                            longRead = false,
                            status = "synced"
                        )
                    ),
                    markAsRead = {},
                    delete = {}
                )
            }
        }
    }
}
