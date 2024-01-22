package com.dbottillo.lifeos.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotionPage(
    @Json(name = "object")
    val obj: String,
    val id: String,
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
    val url: String?,
    val checkbox: Boolean?,
    @Json(name = "multi_select")
    val multiSelect: List<MultiSelectPropertyOption>?,
    @Json(name = "select")
    val select: MultiSelectPropertyOption?,
    @Json(name = "status")
    val status: NotionStatus? = null,
    val date: NotionDate? = null
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
    val status: FilterEqualsRequest? = null,
    val date: FilterBeforeRequest? = null,
    val or: List<FilterRequest>? = null,
    val and: List<FilterRequest>? = null,
    val select: FilterEqualsRequest? = null,
    val checkbox: FilterCheckboxRequest? = null
)

@JsonClass(generateAdapter = true)
class FilterEqualsRequest(
    val equals: String
)

@JsonClass(generateAdapter = true)
class FilterBeforeRequest(
    @Json(name = "on_or_before")
    val onOrBefore: String
)

@JsonClass(generateAdapter = true)
class FilterCheckboxRequest(
    val equals: Boolean
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
class AddPageNotionBodyRequest(
    val parent: AddPageNotionBodyRequestParent,
    val properties: Map<String, AddPageNotionProperty>,
)

@JsonClass(generateAdapter = true)
data class AddPageNotionBodyRequestParent(
    val type: String = "database_id",
    @Json(name = "database_id") val databaseId: String
)

@JsonClass(generateAdapter = true)
data class AddPageNotionProperty(
    @Json(name = "rich_text") val richText: List<AddPageNotionPropertyRichText>? = null,
    val title: List<AddPageNotionPropertyTitle>? = null,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyRichText(
    val text: AddPageNotionPropertyText
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyTitle(
    val text: AddPageNotionPropertyText
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyText(
    val content: String?
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyUrl(
    val url: String
)

@JsonClass(generateAdapter = true)
class ArchiveBodyRequest(
    val archived: Boolean
)

@JsonClass(generateAdapter = true)
class UpdateBodyRequest(
    val properties: Map<String, NotionUpdateProperty>,
)

@JsonClass(generateAdapter = true)
data class NotionUpdateProperty(
    val status: NotionStatus
)

@JsonClass(generateAdapter = true)
data class NotionDate(
    val start: String?,
    val end: String?,
    @Json(name = "time_zone") val timeZone: String?
)
