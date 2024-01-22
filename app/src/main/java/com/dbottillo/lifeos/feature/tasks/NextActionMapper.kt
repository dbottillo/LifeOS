package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import javax.inject.Inject

class NextActionMapper @Inject constructor() {

    fun map(input: List<NotionEntry>): List<NextAction> {
        return input.map { entry ->
            val name = entry.title ?: "No title"
            val emoji = entry.emoji ?: ""
            val text = emoji + name
            NextAction(
                text = text,
                url = entry.url,
                color = entry.color ?: ""
            )
        }
    }
}
