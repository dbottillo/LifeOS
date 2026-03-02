package com.dbottillo.lifeos.ui

import androidx.compose.ui.graphics.Color
import com.dbottillo.lifeos.feature.tasks.Inbox
import com.dbottillo.lifeos.feature.tasks.Parent
import com.dbottillo.lifeos.feature.tasks.Folder
import com.dbottillo.lifeos.feature.tasks.Soon
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.Resource
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Status
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.Date

class EntryContentMappersTest {

    @Test
    fun `mapInbox maps correctly`() {
        val input = listOf(
            Inbox(
                id = "1",
                text = "Inbox Task",
                url = "url",
                due = Date(),
                dueFormatted = "01/01",
                link = "link",
                parent = Parent("p1", "Parent"),
                color = "blue"
            )
        )

        val result = input.mapInbox()

        assertThat(result).hasSize(1)
        with(result[0]) {
            assertThat(id).isEqualTo("1")
            assertThat(displayId).isEqualTo("inbox-1")
            assertThat(title).isEqualTo("Inbox Task")
            assertThat(url).isEqualTo("url")
            assertThat(subtitle).isEqualTo("01/01")
            assertThat(link).isEqualTo("link")
            assertThat(parent).isEqualTo("Parent")
            assertThat(color).isEqualTo(ColorType.Blue.color)
        }
    }

    @Test
    fun `mapFolder maps correctly and handles progress`() {
        val input = listOf(
            Folder(
                id = "1",
                text = "Folder",
                url = "url",
                color = "green",
                due = null,
                dueFormatted = null,
                progress = 0.364f,
                status = Status.Focus,
                link = "link",
                parent = null
            )
        )

        val result = input.mapFolder()

        assertThat(result).hasSize(1)
        assertThat(result[0].subtitle).isEqualTo("36%")
        assertThat(result[0].color).isEqualTo(ColorType.Green.color)
    }

    @Test
    fun `mapFolder handles null progress`() {
        val input = listOf(
            Folder(
                id = "1",
                text = "Folder",
                url = "url",
                color = "green",
                due = null,
                dueFormatted = null,
                progress = null,
                status = Status.Focus,
                link = "link",
                parent = null
            )
        )

        val result = input.mapFolder()

        assertThat(result[0].subtitle).isEqualTo("")
    }

    @Test
    fun `toColor maps correctly`() {
        assertThat("blue".toColor()).isEqualTo(ColorType.Blue.color)
        assertThat("red".toColor()).isEqualTo(ColorType.Red.color)
        assertThat("green".toColor()).isEqualTo(ColorType.Green.color)
        assertThat("unknown".toColor()).isEqualTo(ColorType.Gray.color)
        assertThat(null.toColor()).isEqualTo(ColorType.Gray.color)
    }

    @Test
    fun `mapGoals maps correctly`() {
        val input = listOf(
            Goal(
                id = "1",
                text = "Goal",
                url = "url",
                color = "blue",
                parent = Parent("p1", "Parent"),
                status = Status.Focus
            )
        )

        val result = input.mapGoals()

        assertThat(result[0].color).isEqualTo(ColorType.Aqua.color) // mapGoals uses Aqua hardcoded
        assertThat(result[0].parent).isEqualTo("Parent")
    }
}
