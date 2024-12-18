package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.db.NotionEntryWithParent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.Exception

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
            val dates = entry.notionEntry.toDate()
            NextAction(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = dates?.first,
                dueFormatted = dates?.second,
                link = entry.notionEntry.link,
                isInbox = entry.notionEntry.status == "Inbox",
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapOngoing(input: List<NotionEntryWithParent>): List<Ongoing> {
        return input.map { entry ->
            val dates = entry.notionEntry.toDate()
            Ongoing(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = dates?.first,
                dueFormatted = dates?.second,
                link = entry.notionEntry.link,
                parent = entry.parent.toParent()
            )
        }
    }

    fun mapProjects(input: List<NotionEntryWithParent>): List<Project> {
        return input.map { entry ->
            val dates = entry.notionEntry.toDate()
            Project(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                due = dates?.first,
                dueFormatted = dates?.second,
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

    private fun NotionEntry.toDate(): Pair<Date, String>? {
        return startDate?.let { sD ->
            try {
                inputDateAndTimeFormatter.parse(sD)?.let {
                    it to outputDateAndTimeFormatter.format(it)
                }
            } catch (_: Exception) {
                inputDateFormatter.parse(sD)?.let {
                    it to outputDateFormatter.format(it)
                }
            }
        }
    }
}

fun List<NotionEntry>.toParent(): Parent? {
    val first = this.firstOrNull() ?: return null
    return Parent(
        id = first.uid,
        title = first.toTitle()
    )
}

fun NotionEntry.toTitle(): String {
    val name = title ?: "No title"
    return emoji?.let {
        "$it $name"
    } ?: name
}
