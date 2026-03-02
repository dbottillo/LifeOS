package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.db.NotionEntryWithParent
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class TasksMapperTest {

    private lateinit var dateMapper: NotionEntryDateMapper
    private lateinit var underTest: TasksMapper

    @BeforeEach
    fun setUp() {
        Locale.setDefault(Locale.UK)
        dateMapper = NotionEntryDateMapper()
        underTest = TasksMapper(dateMapper)
    }

    @Test
    fun `mapInbox maps NotionEntryWithParent to Inbox correctly`() {
        val entry = createNotionEntry(uid = "1", title = "Task 1", startDate = "2024-03-01T10:00:00")
        val parent = createNotionEntry(uid = "p1", title = "Parent Project")
        val input = listOf(NotionEntryWithParent(entry, listOf(parent)))

        val result = underTest.mapInbox(input)

        assertThat(result).hasSize(1)
        val inbox = result.first()
        assertThat(inbox.id).isEqualTo("1")
        assertThat(inbox.text).isEqualTo("Task 1")
        assertThat(inbox.dueFormatted).isEqualTo("01/03 10:00")
        assertThat(inbox.parent?.id).isEqualTo("p1")
        assertThat(inbox.parent?.title).isEqualTo("Parent Project")
    }

    @Test
    fun `toTitle includes emoji if present`() {
        val entryWithEmoji = createNotionEntry(title = "Task", emoji = "🚀")
        val entryWithoutEmoji = createNotionEntry(title = "Task", emoji = null)

        assertThat(entryWithEmoji.toTitle()).isEqualTo("🚀 Task")
        assertThat(entryWithoutEmoji.toTitle()).isEqualTo("Task")
    }

    @Test
    fun `NotionEntryDateMapper maps date and time correctly`() {
        val entry = createNotionEntry(startDate = "2024-03-01T15:30:00")

        val result = dateMapper.map(entry)

        assertThat(result?.second).isEqualTo("01/03 15:30")
    }

    @Test
    fun `NotionEntryDateMapper maps date only correctly`() {
        val entry = createNotionEntry(startDate = "2024-03-01")

        val result = dateMapper.map(entry)

        assertThat(result?.second).isEqualTo("01/03")
    }

    @Test
    fun `NotionEntryDateMapper returns null for null startDate`() {
        val entry = createNotionEntry(startDate = null)

        val result = dateMapper.map(entry)

        assertThat(result).isNull()
    }

    private fun createNotionEntry(
        uid: String = "id",
        title: String? = "title",
        emoji: String? = null,
        startDate: String? = null,
        url: String = "url",
        type: String = "type",
        status: String = "status"
    ) = NotionEntry(
        uid = uid,
        title = title,
        emoji = emoji,
        url = url,
        color = null,
        type = type,
        startDate = startDate,
        endDate = null,
        timeZone = null,
        status = status,
        progress = null,
        link = null,
        parentId = null
    )
}
