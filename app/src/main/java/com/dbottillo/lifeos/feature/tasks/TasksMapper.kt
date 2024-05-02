package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class TasksMapper @Inject constructor() {

    private val inputDateAndTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputDateAndTimeFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    private val outputDateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    fun mapIdeas(input: List<NotionEntry>): List<Idea> {
        return input.map { entry ->
            Idea(
                id = entry.uid,
                text = entry.toTitle(),
                url = entry.url,
                link = entry.link
            )
        }
    }

    fun mapNextActions(input: List<NotionEntry>): List<NextAction> {
        return input.map { entry ->
            NextAction(
                id = entry.uid,
                text = entry.toTitle(),
                url = entry.url,
                color = entry.color ?: "",
                due = entry.toDate(),
                link = entry.link,
                isInbox = entry.status == "Inbox"
            )
        }
    }

    fun mapProjects(input: List<NotionEntry>): List<Project> {
        return input.map { entry ->
            Project(
                id = entry.uid,
                text = entry.toTitle(),
                url = entry.url,
                color = entry.color ?: "",
                due = entry.toDate(),
                progress = entry.progress,
                status = entry.status.toStatus(),
                link = entry.link
            )
        }
    }

    fun mapAreas(input: List<NotionEntry>): List<Area> {
        return input.map { entry ->
            Area(
                id = entry.uid,
                text = entry.toTitle(),
                url = entry.url,
                link = entry.link
            )
        }
    }

    fun mapResources(input: List<NotionEntry>): List<Resource> {
        return input.map { entry ->
            Resource(
                id = entry.uid,
                text = entry.toTitle(),
                url = entry.url,
                link = entry.link
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

private fun NotionEntry.toTitle(): String {
    val name = title ?: "No title"
    return emoji?.let {
        "$it $name"
    } ?: name
}
