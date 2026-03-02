package com.dbottillo.lifeos.feature.articles

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.Article
import com.dbottillo.lifeos.db.ArticleDao
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.ApiResult
import com.dbottillo.lifeos.network.NotionDatabaseQueryResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response

class ArticleRepositoryTest {

    private lateinit var api: ApiInterface
    private lateinit var db: AppDatabase
    private lateinit var dao: ArticleDao
    private lateinit var articleManager: ArticleManager
    private lateinit var underTest: ArticleRepository

    @BeforeEach
    fun setUp() {
        api = mock(ApiInterface::class.java)
        db = mock(AppDatabase::class.java)
        dao = mock(ArticleDao::class.java)
        articleManager = mock(ArticleManager::class.java)
        whenever(db.articleDao()).thenReturn(dao)
        underTest = ArticleRepository(api, db, articleManager)
    }

    @Test
    fun `articles returns flow of synced articles`() = runTest {
        val articleList = listOf(Article("1", "title", "url", false, "synced", 1L))
        whenever(dao.getAllSyncedArticles()).thenReturn(flowOf(articleList))

        val result = underTest.articles().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].uid).isEqualTo("1")
    }

    @Test
    fun `deleteArticle updates dao and manager`() = runTest {
        val article = Article("1", "title", "url", false, "synced", 1L)

        underTest.deleteArticle(article)

        verify(dao).updateArticle(article.copy(status = "delete"))
        verify(articleManager).deleteArticle(article)
    }

    @Test
    fun `markArticleAsRead updates dao and manager`() = runTest {
        val article = Article("1", "title", "url", false, "synced", 1L)

        underTest.markArticleAsRead(article)

        verify(dao).updateArticle(article.copy(status = "read"))
        verify(articleManager).updateArticleStatus(article, "Read")
    }

    @Test
    fun `findArticle returns article from dao`() = runTest {
        val article = Article("1", "title", "url", false, "synced", 1L)
        whenever(dao.findArticle("1")).thenReturn(article)

        val result = underTest.findArticle("1")

        assertThat(result).isEqualTo(article)
    }

    @Test
    fun `fetchArticles returns success when api call is successful`() = runTest {
        val queryResult = NotionDatabaseQueryResult(results = emptyList(), nextCursor = null)
        whenever(api.queryDatabase(any(), any())).thenReturn(Response.success(queryResult))

        val result = underTest.fetchArticles()

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        verify(dao).deleteAndInsertAll(emptyList())
    }
}
