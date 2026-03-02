package com.dbottillo.lifeos.feature.home

import com.dbottillo.lifeos.helpers.MainDispatcherExtension
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
import com.google.common.truth.Truth.assertThat
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private lateinit var tasksRepository: TasksRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var refreshProvider: RefreshProvider
    private lateinit var widgetsRefresher: WidgetsRefresher
    private lateinit var weeklyTasksUseCase: WeeklyTasksUseCase
    private lateinit var underTest: HomeViewModel

    @BeforeEach
    fun setUp() {
        tasksRepository = mock(TasksRepository::class.java)
        articleRepository = mock(ArticleRepository::class.java)
        refreshProvider = mock(RefreshProvider::class.java)
        widgetsRefresher = mock(WidgetsRefresher::class.java)
        weeklyTasksUseCase = mock(WeeklyTasksUseCase::class.java)

        whenever(tasksRepository.focusFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(tasksRepository.inboxFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(tasksRepository.foldersFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(weeklyTasksUseCase.flow).thenReturn(MutableStateFlow(emptyMap()))
        whenever(articleRepository.articles()).thenReturn(MutableStateFlow(emptyList()))

        underTest = HomeViewModel(
            tasksRepository,
            articleRepository,
            refreshProvider,
            widgetsRefresher,
            weeklyTasksUseCase
        )
    }

    @Test
    fun `reloadHome success triggers refresh and refreshes widgets`() = runTest(testDispatcher) {
        whenever(tasksRepository.loadNextActions()).thenReturn(Result.success(Unit))

        underTest.homeState.test {
            assertThat(awaitItem().refreshing).isFalse()

            underTest.reloadHome()

            assertThat(awaitItem().refreshing).isTrue()
            assertThat(awaitItem().refreshing).isFalse()

            verify(widgetsRefresher).refreshAll()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reloadHome failure updates state with error`() = runTest(testDispatcher) {
        val error = Throwable("error")
        whenever(tasksRepository.loadNextActions()).thenReturn(Result.failure(error))

        underTest.homeState.test {
            assertThat(awaitItem().refreshing).isFalse()

            underTest.reloadHome()

            assertThat(awaitItem().refreshing).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.refreshing).isFalse()
            assertThat(errorState.nonBlockingError).isEqualTo(error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load triggers refreshProvider`() {
        underTest.load()
        verify(refreshProvider).immediate()
    }
}
