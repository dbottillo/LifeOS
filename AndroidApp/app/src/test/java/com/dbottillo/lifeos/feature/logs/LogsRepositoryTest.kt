package com.dbottillo.lifeos.feature.logs

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.Log
import com.dbottillo.lifeos.db.LogDao
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

class LogsRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: LogDao
    private lateinit var underTest: LogsRepository

    @BeforeEach
    fun setUp() {
        db = mock(AppDatabase::class.java)
        dao = mock(LogDao::class.java)
        whenever(db.logDao()).thenReturn(dao)
        underTest = LogsRepository(db)
    }

    @Test
    fun `entries returns flow of logs`() = runTest {
        val logList = listOf(Log(1, "tag", "info", "message", 123L))
        whenever(dao.getAll()).thenReturn(flowOf(logList))

        val result = underTest.entries().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].tag).isEqualTo("tag")
        assertThat(result[0].message).isEqualTo("message")
    }

    @Test
    fun `addEntry inserts new log into dao`() = runTest {
        underTest.addEntry(LogTags.HOME_REFRESH, LogLevel.INFO, "test message")

        verify(dao).insert(any())
    }
}
