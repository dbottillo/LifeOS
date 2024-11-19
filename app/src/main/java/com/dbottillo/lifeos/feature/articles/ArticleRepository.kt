package com.dbottillo.lifeos.feature.articles

import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.ApiResult
import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val api: ApiInterface,
    private val db: AppDatabase,
    private val articleManager: ArticleManager
) {

    fun articles(): Flow<List<Article>> {
        return db.articleDao().getAllSyncedArticles()
    }

    suspend fun deleteArticle(article: Article) {
        withContext(Dispatchers.IO) {
            db.articleDao().updateArticle(article.copy(status = "delete"))
            articleManager.deleteArticle(article)
        }
    }

    suspend fun markArticleAsRead(article: Article) {
        withContext(Dispatchers.IO) {
            db.articleDao().updateArticle(article.copy(status = "read"))
            articleManager.updateArticleStatus(article, "Read")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun fetchArticles(): ApiResult<Unit> = coroutineScope {
        try {
            val request = NotionBodyRequest(
                filter = FilterRequest(
                    or = listOf(
                        FilterRequest(
                            property = "Status",
                            status = FilterEqualsRequest(
                                equals = "Inbox"
                            )
                        ),
                        FilterRequest(
                            property = "Status",
                            status = FilterEqualsRequest(
                                equals = "Long read"
                            )
                        )
                    )
                ),
                sorts = emptyList()
            )
            val response = api.queryDatabase(AppConstant.ARTICLES_DATABASE_ID, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val articles = body.results.map {
                        Article(
                            uid = it.id,
                            url = it.properties["URL"]?.url ?: "",
                            longRead = it.properties["Status"]?.status?.name == "Long read",
                            title = it.properties["Name"]?.title?.getOrNull(0)?.plainText ?: "",
                            status = "synced"
                        )
                    }
                    db.articleDao().deleteAndInsertAll(articles)
                }
                ApiResult.Success(Unit)
            } else {
                val throwable = Throwable("${response.code()} ${response.message()}")
                Firebase.crashlytics.recordException(throwable)
                ApiResult.Error(throwable)
            }
        } catch (e: Exception) {
            val throwable = Throwable(e.message ?: e.toString())
            Firebase.crashlytics.recordException(throwable)
            ApiResult.Error(throwable)
        }
    }

    suspend fun findArticle(uuid: String): Article {
        return db.articleDao().findArticle(uuid = uuid)
    }
}
