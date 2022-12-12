package com.dbottillo.notionalert

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotionPage(
    @Json(name = "object")
    val obj: String,
    val icon: NotionPageIcon?,
    val properties: Map<String, NotionProperty>
)

@JsonClass(generateAdapter = true)
data class NotionPageIcon(
    val type: String?,
    val emoji: String?
)

@JsonClass(generateAdapter = true)
data class NotionProperty(
    val id: String,
    val type: String,
    val title: List<NotionTitle>?,
    val checkbox: Boolean?,
    @Json(name = "multi_select")
    val multiSelect: List<MultiSelectPropertyOption>?
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
class NotionDatabaseQueryResult(
    val results: List<NotionPage>
)

@JsonClass(generateAdapter = true)
class NotionDatabase(
    val id: String,
    val properties: Map<String, NotionDatabaseProperty>
)

@JsonClass(generateAdapter = true)
data class NotionDatabaseProperty(
    val id: String,
    val name: String,
    @Json(name = "multi_select")
    val multiSelect: List<MultiSelectProperty>?
)

@JsonClass(generateAdapter = true)
data class MultiSelectProperty(
    val options: List<MultiSelectPropertyOption>
)

@JsonClass(generateAdapter = true)
data class MultiSelectPropertyOption(
    val id: String,
    val name: String,
    val color: String
)

@JsonClass(generateAdapter = true)
data class PocketGetResult(
    val status: Int,
    val list: Map<String, Any>
)
