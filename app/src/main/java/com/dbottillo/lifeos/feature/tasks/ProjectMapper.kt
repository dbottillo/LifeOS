package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ProjectMapper @Inject constructor() {

    private val inputDateAndTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    fun map(input: List<NotionEntry>): List<Project> {
        return input.map { entry ->
            val name = entry.title ?: "No title"
            val text = entry.emoji?.let {
                "$it $name"
            } ?: name
            val date: String? = try {
                entry.startDate?.let { inputDateAndTimeFormatter.parse(it) }?.let {
                    dateFormatter.format(it)
                }
            } catch (_: Exception) {
                entry.startDate?.let { inputDateFormatter.parse(it) }?.let {
                    dateFormatter.format(it)
                }
            }
            Project(
                text = text,
                url = entry.url,
                color = entry.color ?: "",
                due = date ?: "",
                progress = entry.progress,
                status = entry.status.toStatus()
            )
        }
    }
}
