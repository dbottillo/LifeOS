package com.dbottillo.lifeos.feature.composer

import com.dbottillo.lifeos.helpers.MainDispatcherExtension
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.tasks.NotionEntryDateMapper
import com.dbottillo.lifeos.feature.tasks.TaskManager
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.google.common.truth.Truth.assertThat
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TaskComposerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private lateinit var articleManager: ArticleManager
    private lateinit var taskManager: TaskManager
    private lateinit var tasksRepository: TasksRepository
    private lateinit var notionEntryDateMapper: NotionEntryDateMapper
    private lateinit var getLocalNotionParentsUseCase: GetLocalNotionParentsUseCase
    private lateinit var underTest: TaskComposerViewModel

    @BeforeEach
    fun setUp() {
        articleManager = mock(ArticleManager::class.java)
        taskManager = mock(TaskManager::class.java)
        tasksRepository = mock(TasksRepository::class.java)
        notionEntryDateMapper = mock(NotionEntryDateMapper::class.java)
        getLocalNotionParentsUseCase = mock(GetLocalNotionParentsUseCase::class.java)

        whenever(getLocalNotionParentsUseCase(any())).thenReturn(flowOf(emptyList()))

        underTest = TaskComposerViewModel(
            articleManager,
            taskManager,
            tasksRepository,
            notionEntryDateMapper,
            getLocalNotionParentsUseCase
        )
    }

    @Test
    fun `init with null entryId sets state to Data`() = runTest(testDispatcher) {
        val input = ComposerInput(entryId = null, title = "title", url = "url")
        
        underTest.init(input)

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.title).isEqualTo("title")
            assertThat(state.link).isEqualTo("url")
            assertThat(state.entryId).isNull()
        }
    }

    @Test
    fun `saveArticle calls articleManager and finishes`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null, title = "title", url = "url"))
        
        underTest.saveArticle()

        verify(articleManager).addArticle("title", "url")
        assertThat(underTest.events.receive()).isEqualTo(ComposerEvents.Finish)
    }

    @Test
    fun `onTitleChange updates state`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null))
        
        underTest.onTitleChange("new title")

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.title).isEqualTo("new title")
        }
    }
}

private fun <T> any(): T = org.mockito.kotlin.any()
