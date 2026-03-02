package com.dbottillo.lifeos.feature.composer

import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GetLocalNotionParentsUseCaseTest {

    private lateinit var tasksRepository: TasksRepository
    private lateinit var underTest: GetLocalNotionParentsUseCase

    @BeforeEach
    fun setUp() {
        tasksRepository = mock(TasksRepository::class.java)
        underTest = GetLocalNotionParentsUseCase(tasksRepository)
    }

    @Test
    fun `invoke calls tasksRepository searchParents`() = runTest {
        val query = "query"
        whenever(tasksRepository.searchParents(query)).thenReturn(flowOf(emptyList()))

        val result = underTest(query).first()

        assertThat(result).isEmpty()
    }
}
