package com.dbottillo.lifeos.feature.review

import com.dbottillo.lifeos.helpers.MainDispatcherExtension
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
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
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private lateinit var tasksRepository: TasksRepository
    private lateinit var goalsRepository: GoalsRepository
    private lateinit var widgetsRefresher: WidgetsRefresher
    private lateinit var underTest: ReviewViewModel

    @BeforeEach
    fun setUp() {
        tasksRepository = mock(TasksRepository::class.java)
        goalsRepository = mock(GoalsRepository::class.java)
        widgetsRefresher = mock(WidgetsRefresher::class.java)

        whenever(tasksRepository.areasFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(tasksRepository.foldersFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(tasksRepository.resourcesFlow).thenReturn(MutableStateFlow(emptyList()))
        whenever(goalsRepository.goalsFlow).thenReturn(MutableStateFlow(emptyList()))

        underTest = ReviewViewModel(tasksRepository, goalsRepository, widgetsRefresher)
    }

    @Test
    fun `reloadReview success triggers refresh and refreshes widgets`() = runTest(testDispatcher) {
        whenever(tasksRepository.loadStaticResources(any())).thenReturn(Result.success(Unit))

        underTest.reviewState.test {
            assertThat(awaitItem().refreshing).isFalse()

            underTest.reloadReview()

            assertThat(awaitItem().refreshing).isTrue()
            assertThat(awaitItem().refreshing).isFalse()

            verify(widgetsRefresher).refreshAll()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `bottomSelection updates otherStateBottomSelection and triggers combined flow`() = runTest(testDispatcher) {
        underTest.reviewState.test {
            awaitItem() // initial

            underTest.bottomSelection(ReviewBottomType.RESOURCES)

            val updatedState = awaitItem()
            assertThat(updatedState.bottom.selection.find { it.type == ReviewBottomType.RESOURCES }?.selected).isTrue()
            assertThat(updatedState.bottom.selection.find { it.type == ReviewBottomType.FOLDERS }?.selected).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T> any(): T = org.mockito.kotlin.any()
