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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.title).isEqualTo("title")
            assertThat(state.link).isEqualTo("url")
            assertThat(state.entryId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveArticle calls articleManager and finishes`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null, title = "title", url = "url"))
        advanceUntilIdle()

        underTest.events.receiveAsFlow().test {
            underTest.saveArticle()
            assertThat(awaitItem()).isEqualTo(ComposerEvents.Finish)
        }

        verify(articleManager).addArticle("title", "url")
    }

    @Test
    fun `onTitleChange updates state`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null))
        advanceUntilIdle()

        underTest.onTitleChange("new title")
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.title).isEqualTo("new title")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDateSelected clears time and updates state`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null))
        advanceUntilIdle()

        val date = 123456789L
        underTest.onDateSelected(date)
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.selectedDueDate).isEqualTo(date)
            assertThat(state.selectedDueTime).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDateAndTimeSelected updates state with both`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null))
        advanceUntilIdle()

        val date = 123456789L
        underTest.onDateAndTimeSelected(date, 14, 30)
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.selectedDueDate).isEqualTo(date)
            assertThat(state.selectedDueTime).isEqualTo(14 to 30)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onClearTime removes time from state`() = runTest(testDispatcher) {
        underTest.init(ComposerInput(entryId = null))
        advanceUntilIdle()

        underTest.onDateAndTimeSelected(123456789L, 14, 30)
        underTest.onClearTime()
        advanceUntilIdle()

        underTest.state.test {
            val state = awaitItem() as ComposerState.Data
            assertThat(state.selectedDueTime).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T> any(): T = org.mockito.kotlin.any()
