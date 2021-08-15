package com.dbottillo.notionalert

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotionPage(
    @Json(name = "object")
    val obj: String,
    val properties: Map<String, NotionProperty>
)

@JsonClass(generateAdapter = true)
data class NotionProperty(
    val id: String,
    val type: String,
    val checkbox: Boolean?,
    val title: List<NotionTitle>?
)

@JsonClass(generateAdapter = true)
data class NotionTitle(
    val type: String,
    @Json(name = "plain_text")
    val plainText: String
)

@JsonClass(generateAdapter = true)
class FilterRequest(
    val filter: Map<String, Any> = mapOf(
        "and" to listOf(
            mapOf(
                "property" to "Status",
                "select" to mapOf(
                    "equals" to "Next Actions"
                )
            ),
            mapOf(
                "property" to "Archived",
                "checkbox" to mapOf(
                    "equals" to false
                )
            )
        )
    )
)

@JsonClass(generateAdapter = true)
class NotionDatabase(
    val results: List<NotionPage>
)
