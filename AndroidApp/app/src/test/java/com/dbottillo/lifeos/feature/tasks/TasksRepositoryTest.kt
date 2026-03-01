package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.NotionEntryDao
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.network.NotionDatabaseQueryResult
import com.dbottillo.lifeos.notification.NotificationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class TasksRepositoryTest {

    private lateinit var api: ApiInterface
    private lateinit var notificationProvider: NotificationProvider
    private lateinit var db: AppDatabase
    private lateinit var dao: NotionEntryDao
    private lateinit var mapper: TasksMapper
    private lateinit var logsRepository: LogsRepository
    private lateinit var underTest: TasksRepository

    @BeforeEach
    fun setUp() {
        api = mock(ApiInterface::class.java)
        notificationProvider = mock(NotificationProvider::class.java)
        db = mock(AppDatabase::class.java)
        dao = mock(NotionEntryDao::class.java)
        whenever(db.notionEntryDao()).thenReturn(dao)
        
        // Mocking all flows to avoid NPE during repository initialization
        whenever(dao.getInbox()).thenReturn(flowOf(emptyList()))
        whenever(dao.getFocus()).thenReturn(flowOf(emptyList()))
        whenever(dao.getFolders()).thenReturn(flowOf(emptyList()))
        whenever(dao.getAreas()).thenReturn(flowOf(emptyList()))
        whenever(dao.getResources()).thenReturn(flowOf(emptyList()))
        whenever(dao.getNextWeek()).thenReturn(flowOf(emptyList()))

        mapper = mock(TasksMapper::class.java)
        logsRepository = mock(LogsRepository::class.java)
        underTest = TasksRepository(api, notificationProvider, db, mapper, logsRepository)
    }

    @Test
    fun `loadNextActions success fetches data and stores it`() = runTest {
        val queryResult = NotionDatabaseQueryResult(results = emptyList(), nextCursor = null)
        whenever(api.queryDatabase(any(), any())).thenReturn(Response.success(queryResult))

        val result = underTest.loadNextActions()

        assertThat(result.isSuccess).isTrue()
        verify(dao).deleteAndSaveFocusInboxNextWeek(emptyList())
    }

    @Test
    fun `loadNextActions failure returns failure`() = runTest {
        val responseBody = mock(okhttp3.ResponseBody::class.java)
        whenever(api.queryDatabase(any(), any())).thenReturn(Response.error<NotionDatabaseQueryResult>(500, responseBody))

        val result = underTest.loadNextActions()

        assertThat(result.isFailure).isTrue()
    }
}
