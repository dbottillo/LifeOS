package com.dbottillo.notionalert

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotionPage(
    @Json(name = "object")
    val obj: String,
    val icon: NotionPageIcon?,
    val properties: Map<String, NotionProperty>,
    val url: String
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
    val multiSelect: List<MultiSelectPropertyOption>?,
    @Json(name = "select")
    val select: MultiSelectPropertyOption?,
    @Json(name = "status")
    val status: NotionStatus? = null
)

@JsonClass(generateAdapter = true)
data class NotionTitle(
    val type: String,
    @Json(name = "plain_text")
    val plainText: String
)

@JsonClass(generateAdapter = true)
data class NotionStatus(
    val name: String
)

@JsonClass(generateAdapter = true)
class NotionBodyRequest(
    val filter: FilterRequest,
    val sorts: List<SortRequest>
)

@JsonClass(generateAdapter = true)
class FilterRequest(
    val property: String? = null,
    val status: FilterSelectRequest? = null,
    val date: FilterBeforeRequest? = null,
    val or: List<FilterRequest>? = null,
    val and: List<FilterRequest>? = null,
    val select: FilterSelectRequest? = null
)

@JsonClass(generateAdapter = true)
class FilterSelectRequest(
    val equals: String
)

@JsonClass(generateAdapter = true)
class FilterBeforeRequest(
    val before: String
)

@JsonClass(generateAdapter = true)
class SortRequest(
    val property: String,
    val direction: String
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
    val multiSelect: List<MultiSelectProperty>?,
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
