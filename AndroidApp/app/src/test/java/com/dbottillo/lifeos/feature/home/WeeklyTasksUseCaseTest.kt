package com.dbottillo.lifeos.feature.home

import com.dbottillo.lifeos.feature.tasks.Soon
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.google.common.truth.Truth.assertThat
import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyTasksUseCaseTest {

    private lateinit var tasksRepository: TasksRepository
    private lateinit var underTest: WeeklyTasksUseCase
    private val soonFlow = MutableStateFlow<List<Soon>>(emptyList())

    @BeforeEach
    fun setUp() {
        Locale.setDefault(Locale.UK)
        tasksRepository = mock(TasksRepository::class.java)
        whenever(tasksRepository.soonFlow).thenReturn(soonFlow)
        underTest = WeeklyTasksUseCase(tasksRepository)
    }

    @Test
    fun `flow groups tasks by formatted date`() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 1, 10, 0)
        val date1 = calendar.time // Friday 01/03

        calendar.set(2024, Calendar.MARCH, 2, 11, 0)
        val date2 = calendar.time // Saturday 02/03

        val soonList = listOf(
            createSoon(id = "1", text = "Task 1", due = date1, dueFormatted = "01/03 10:00"),
            createSoon(id = "2", text = "Task 2", due = date1, dueFormatted = "01/03 10:00"),
            createSoon(id = "3", text = "Task 3", due = date2, dueFormatted = "02/03 11:00")
        )

        underTest.flow.test {
            soonFlow.emit(soonList)
            val result = awaitItem()
            if (result.isEmpty()) {
                val nextResult = awaitItem()
                assertThat(nextResult).hasSize(2)
                assertThat(nextResult.keys).containsExactly("Friday 01/03", "Saturday 02/03")
                assertThat(nextResult["Friday 01/03"]).hasSize(2)
                assertThat(nextResult["Saturday 02/03"]).hasSize(1)
            } else {
                assertThat(result).hasSize(2)
                assertThat(result.keys).containsExactly("Friday 01/03", "Saturday 02/03")
                assertThat(result["Friday 01/03"]).hasSize(2)
                assertThat(result["Saturday 02/03"]).hasSize(1)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `flow handles tasks without due date`() = runTest {
        val soonList = listOf(
            createSoon(id = "1", text = "Task 1", due = null)
        )

        underTest.flow.test {
            soonFlow.emit(soonList)
            val result = awaitItem()
            if (result.isEmpty()) {
                val nextResult = awaitItem()
                assertThat(nextResult).hasSize(1)
                assertThat(nextResult.keys).containsExactly("No date")
            } else {
                assertThat(result).hasSize(1)
                assertThat(result.keys).containsExactly("No date")
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createSoon(
        id: String,
        text: String,
        due: Date?,
        dueFormatted: String? = null
    ) = Soon(
        id = id,
        text = text,
        url = "url",
        color = "blue",
        due = due,
        dueFormatted = dueFormatted,
        link = null,
        parent = null
    )
}
