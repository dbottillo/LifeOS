package com.dbottillo.lifeos.feature.status

import com.dbottillo.lifeos.helpers.MainDispatcherExtension
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.dbottillo.lifeos.network.RefreshProvider
import com.dbottillo.lifeos.notification.NotificationProvider
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
class StatusViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private lateinit var tasksRepository: TasksRepository
    private lateinit var notificationProvider: NotificationProvider
    private lateinit var refreshProvider: RefreshProvider
    private lateinit var articleManager: ArticleManager
    private lateinit var logsRepository: LogsRepository
    private lateinit var widgetsRefresher: WidgetsRefresher
    private lateinit var underTest: StatusViewModel

    @BeforeEach
    fun setUp() {
        tasksRepository = mock(TasksRepository::class.java)
        notificationProvider = mock(NotificationProvider::class.java)
        refreshProvider = mock(RefreshProvider::class.java)
        articleManager = mock(ArticleManager::class.java)
        logsRepository = mock(LogsRepository::class.java)
        widgetsRefresher = mock(WidgetsRefresher::class.java)

        whenever(refreshProvider.periodicStatus()).thenReturn(MutableStateFlow(emptyList()))
        whenever(refreshProvider.dailyStatus()).thenReturn(MutableStateFlow(emptyList()))
        whenever(articleManager.status()).thenReturn(MutableStateFlow(emptyList()))
        whenever(logsRepository.entries()).thenReturn(MutableStateFlow(emptyList()))

        underTest = StatusViewModel(
            tasksRepository,
            notificationProvider,
            refreshProvider,
            articleManager,
            logsRepository,
            widgetsRefresher
        )
    }

    @Test
    fun `reloadAll success triggers refresh and refreshes widgets`() = runTest(testDispatcher) {
        whenever(tasksRepository.loadStaticResources(any())).thenReturn(Result.success(Unit))

        underTest.state.test {
            assertThat(awaitItem().allLoading).isFalse()

            underTest.reloadAll()

            assertThat(awaitItem().allLoading).isTrue()
            assertThat(awaitItem().allLoading).isFalse()
            
            verify(widgetsRefresher).refreshAll()
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load calls refreshProvider immediate`() {
        underTest.load()
        verify(refreshProvider).immediate()
    }

    @Test
    fun `stop clears notification and stops refreshProvider`() {
        underTest.stop()
        verify(notificationProvider).clear()
        verify(refreshProvider).stop()
    }
}

private fun <T> any(): T = org.mockito.kotlin.any()
