package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.db.NotionEntryWithParent
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class TasksMapper @Inject constructor() {

    private val inputDateAndTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputDateAndTimeFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    private val outputDateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun mapIdeas(input: List<NotionEntryWithParent>): List<Idea> {
        return input.map { entry ->
            Idea(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                link = entry.notionEntry.link,
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapNextActions(input: List<NotionEntryWithParent>): List<NextAction> {
        return input.map { entry ->
            NextAction(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = entry.notionEntry.toDate(),
                link = entry.notionEntry.link,
                isInbox = entry.notionEntry.status == "Inbox",
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapBlocked(input: List<NotionEntryWithParent>): List<Blocked> {
        return input.map { entry ->
            Blocked(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = entry.notionEntry.toDate(),
                link = entry.notionEntry.link,
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapProjects(input: List<NotionEntryWithParent>): List<Project> {
        return input.map { entry ->
            Project(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = entry.notionEntry.toDate(),
                progress = entry.notionEntry.progress,
                status = entry.notionEntry.status.toStatus(),
                link = entry.notionEntry.link,
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapAreas(input: List<NotionEntryWithParent>): List<Area> {
        return input.map { entry ->
            Area(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                link = entry.notionEntry.link
            )
        }
    }

    fun mapResources(input: List<NotionEntryWithParent>): List<Resource> {
        return input.map { entry ->
            Resource(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                link = entry.notionEntry.link,
                parent = entry.parent.toParent()
            )
        }
    }

    private fun NotionEntry.toDate(): String {
        val date: String? = try {
            startDate?.let { inputDateAndTimeFormatter.parse(it) }?.let {
                outputDateAndTimeFormatter.format(it)
            }
        } catch (_: Exception) {
            startDate?.let { inputDateFormatter.parse(it) }?.let {
                outputDateFormatter.format(it)
            }
        }
        return date ?: ""
    }
}

private fun List<NotionEntry>.toParent(): Parent? {
    val first = this.firstOrNull() ?: return null
    return Parent(
        id = first.uid,
        title = first.toTitle()
    )
}

private fun NotionEntry.toTitle(): String {
    val name = title ?: "No title"
    return emoji?.let {
        "$it $name"
    } ?: name
}
