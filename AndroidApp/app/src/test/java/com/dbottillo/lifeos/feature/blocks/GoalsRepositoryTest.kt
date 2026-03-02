package com.dbottillo.lifeos.feature.blocks

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.db.NotionEntryDao
import com.dbottillo.lifeos.db.NotionEntryWithParent
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Status
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GoalsRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NotionEntryDao
    private lateinit var mapper: GoalsMapper
    private lateinit var underTest: GoalsRepository

    @BeforeEach
    fun setUp() {
        db = mock(AppDatabase::class.java)
        dao = mock(NotionEntryDao::class.java)
        whenever(db.notionEntryDao()).thenReturn(dao)
        mapper = GoalsMapper()
    }

    @Test
    fun `goalsFlow returns mapped goals`() = runTest {
        val entry = NotionEntry(
            uid = "1",
            title = "Goal 1",
            emoji = null,
            url = "url",
            color = "blue",
            type = "Goal",
            startDate = null,
            endDate = null,
            timeZone = null,
            status = "Focus",
            progress = null,
            link = null,
            parentId = null
        )
        val entryWithParent = NotionEntryWithParent(entry, emptyList())
        whenever(dao.getGoals()).thenReturn(flowOf(listOf(entryWithParent)))

        underTest = GoalsRepository(db, mapper)

        val result = underTest.goalsFlow.first()
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("1")
        assertThat(result[0].text).isEqualTo("Goal 1")
        assertThat(result[0].status).isEqualTo(Status.Focus)
    }
}
