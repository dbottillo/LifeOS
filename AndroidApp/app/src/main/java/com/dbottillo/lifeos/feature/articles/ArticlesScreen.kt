package com.dbottillo.lifeos.feature.articles

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.feature.home.HomeViewModel
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import java.util.UUID

@Composable
fun ArticlesScreen(viewModel: HomeViewModel) {
    val state = viewModel.articleState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        ArticlesScreenExpanded(
            inbox = state.value.articlesData.inbox,
            longRead = state.value.articlesData.longRead,
            markAsRead = viewModel::markAsRead,
            delete = viewModel::delete,
        )
    } else {
        ArticlesScreenContent(
            inbox = state.value.articlesData.inbox,
            longRead = state.value.articlesData.longRead,
            markAsRead = viewModel::markAsRead,
            delete = viewModel::delete,
            open = {
                context.openLink(it.url)
            }
        )
    }
}

@Composable
fun ArticlesScreenExpanded(
    inbox: List<Article>,
    longRead: List<Article>,
    markAsRead: (Article) -> Unit,
    delete: (Article) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var url by rememberSaveable { mutableStateOf("") }
        ArticlesScreenContent(
            modifier = Modifier.weight(0.3f),
            inbox = inbox,
            longRead = longRead,
            markAsRead = markAsRead,
            delete = delete,
            open = {
                url = it.url
            }
        )
        Box(
            modifier = Modifier.weight(0.7f),
        ) {
            if (url.isNotEmpty()) {
                ArticleContentWebView(
                    url = url,
                )
            }
        }
    }
}

@Composable
fun ArticlesScreenContent(
    modifier: Modifier = Modifier,
    inbox: List<Article>,
    longRead: List<Article>,
    markAsRead: (Article) -> Unit,
    open: (Article) -> Unit,
    delete: (Article) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (inbox.isNotEmpty()) {
            item(key = "inbox") {
                Section("Inbox (${inbox.size})",)
            }
            inbox.forEachIndexed { index, article ->
                item(key = article.uid) {
                    Article(article, index, markAsRead, open, delete)
                }
            }
        }
        if (longRead.isNotEmpty()) {
            item(key = "long read") {
                Section("Long read (${longRead.size})")
            }
            longRead.forEachIndexed { index, article ->
                item(key = article.uid) {
                    Article(article, index, markAsRead, open, delete)
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
    open: (Article) -> Unit,
    delete: (Article) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable {
                open.invoke(article)
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

@Composable
fun ArticleContentWebView(
    modifier: Modifier = Modifier,
    url: String
) {
    val context = LocalContext.current
    val webView = remember {
            WebView(context).apply {
            this.settings.javaScriptEnabled = true
        }
    }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(url) {
        isLoading = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                isLoading = false
            }
        }
        webView.clipToOutline = true
        webView.loadUrl(url)
    }
    Box(modifier.fillMaxSize()) {
        AndroidView(factory = { webView }, onReset = {})
        if (isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                            status = "synced",
                            createdAt = 1L
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 2",
                            url = "article url 2",
                            longRead = false,
                            status = "synced",
                            createdAt = 2L
                        )
                    ),
                    longRead = listOf(
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 3",
                            url = "article url",
                            longRead = false,
                            status = "synced",
                            createdAt = 3L
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 4",
                            url = "article url 2",
                            longRead = false,
                            status = "synced",
                            createdAt = 4L
                        ),
                        Article(
                            uid = UUID.randomUUID().toString(),
                            title = "Article title 5",
                            url = "article url 2",
                            longRead = false,
                            status = "synced",
                            createdAt = 5L
                        )
                    ),
                    markAsRead = {},
                    delete = {},
                    open = {}
                )
            }
        }
    }
}
