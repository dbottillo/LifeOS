package com.dbottillo.lifeos.ui

import androidx.compose.ui.graphics.Color
import com.dbottillo.lifeos.feature.tasks.Area
import com.dbottillo.lifeos.feature.tasks.Focus
import com.dbottillo.lifeos.feature.tasks.Folder
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Idea
import com.dbottillo.lifeos.feature.tasks.Inbox
import com.dbottillo.lifeos.feature.tasks.NextWeek
import com.dbottillo.lifeos.feature.tasks.Resource

data class EntryContent(
    val id: String,
    val displayId: String,
    val title: String,
    val url: String,
    val subtitle: String? = null,
    val link: String? = null,
    val parent: String? = null,
    val color: Color
)

fun List<Inbox>.mapInbox(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "inbox-${it.id}",
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Focus>.mapFocus(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "focus-${it.id}",
            title = it.text,
            url = it.url,
            subtitle = it.dueFormatted,
            link = it.link,
            parent = it.parent?.title,
            color = it.color.toColor()
        )
    }
}

fun List<Folder>.mapFolder(): List<EntryContent> {
    return map {
        val subtitle = it.progress?.let { "${(it * 100).toInt()}%" } ?: ""
        EntryContent(
            id = it.id,
            displayId = "folder-${it.id}",
            title = it.text,
            subtitle = subtitle,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Green.color
        )
    }
}

fun List<NextWeek>.mapNextWeek(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "next-week-${it.id}",
            title = it.text,
            subtitle = it.dueFormatted,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Orange.color
        )
    }
}

fun List<Area>.mapAreas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "area-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            color = ColorType.Yellow.color
        )
    }
}

fun List<Idea>.mapIdeas(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "idea-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Orange.color
        )
    }
}

fun List<Resource>.mapResources(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "resource-${it.id}",
            title = it.text,
            url = it.url,
            link = it.link,
            parent = it.parent?.title,
            color = ColorType.Purple.color
        )
    }
}

fun List<Goal>.mapGoals(): List<EntryContent> {
    return map {
        EntryContent(
            id = it.id,
            displayId = "goal-${it.id}",
            title = it.text,
            url = it.url,
            parent = it.parent?.title,
            color = ColorType.Aqua.color
        )
    }
}

fun String?.toColor(): Color {
    return when (this) {
        "gray" -> ColorType.Gray.color
        "orange" -> ColorType.Orange.color
        "green" -> ColorType.Green.color
        "blue" -> ColorType.Blue.color
        "red" -> ColorType.Red.color
        "purple" -> ColorType.Purple.color
        "pink" -> ColorType.Pink.color
        "yellow" -> ColorType.Yellow.color
        else -> ColorType.Gray.color
    }
}

@Suppress("MagicNumber")
enum class ColorType(val color: Color) {
    Gray(Color(0xFF777777)),
    Orange(Color(0xFF8D4A13)),
    Green(Color(0xFF2C6845)),
    Blue(Color(0xFF183A69)),
    Red(Color(0xFF751E1E)),
    Purple(Color(0xFF634681)),
    Pink(Color(0xFF8B1E77)),
    Yellow(Color(0xFF7E7317)),
    Aqua(Color(0xFF00535D)),
}
