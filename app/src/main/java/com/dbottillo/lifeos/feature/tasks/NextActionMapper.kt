package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class NextActionMapper @Inject constructor() {

    private val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    fun map(input: List<NotionEntry>): List<NextAction> {
        return input.map { entry ->
            val name = entry.title ?: "No title"
            val emoji = entry.emoji ?: ""
            val text = emoji + name
            NextAction(
                text = text,
                url = entry.url,
                color = entry.color ?: "",
                due = entry.startDate?.let {
                    inputDateFormatter.parse(it)
                    ?.let { it1 -> dateFormatter.format(it1) }
                } ?: ""
            )
        }
    }
}
