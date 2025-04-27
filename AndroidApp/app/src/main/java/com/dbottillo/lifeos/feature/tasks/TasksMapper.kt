package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import com.dbottillo.lifeos.db.NotionEntryWithParent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

class TasksMapper @Inject constructor(
    private val dateMapper: NotionEntryDateMapper
) {

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

    fun mapInbox(input: List<NotionEntryWithParent>): List<Inbox> {
        return input.map { entry ->
            val dates = dateMapper.map(entry.notionEntry)
            Inbox(
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

    fun mapFocus(input: List<NotionEntryWithParent>): List<Focus> {
        return input.map { entry ->
            val dates = dateMapper.map(entry.notionEntry)
            Focus(
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

    fun mapSoon(input: List<NotionEntryWithParent>): List<Soon> {
        return input.map { entry ->
            val dates = dateMapper.map(entry.notionEntry)
            Soon(
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

    fun mapFolders(input: List<NotionEntryWithParent>): List<Folder> {
        return input.map { entry ->
            val dates = dateMapper.map(entry.notionEntry)
            Folder(
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
}

@Singleton
class NotionEntryDateMapper @Inject constructor() {
    private val inputDateAndTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputDateAndTimeFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    private val outputDateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun map(entry: NotionEntry): Pair<Date, String>? {
        return entry.startDate?.let { sD ->
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
