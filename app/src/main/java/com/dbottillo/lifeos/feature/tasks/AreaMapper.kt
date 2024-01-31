package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.db.NotionEntry
import javax.inject.Inject

class AreaMapper @Inject constructor() {

    fun map(input: List<NotionEntry>): List<Area> {
        return input.map { entry ->
            val name = entry.title ?: "No title"
            val text = entry.emoji?.let {
                "$it $name"
            } ?: name
            Area(
                text = text,
                url = entry.url,
            )
        }
    }
}
