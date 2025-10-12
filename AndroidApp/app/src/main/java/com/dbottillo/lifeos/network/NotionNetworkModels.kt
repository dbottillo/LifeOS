package com.dbottillo.lifeos.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotionPage(
    @param:Json(name = "object")
    val obj: String,
    val id: String,
    val icon: NotionPageIcon?,
    val properties: Map<String, NotionProperty>,
    val url: String,
    @param:Json(name = "created_time")
    val createdTime: String,
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
    @param:Json(name = "multi_select")
    val multiSelect: List<MultiSelectPropertyOption>?,
    @param:Json(name = "select")
    val select: MultiSelectPropertyOption?,
    @param:Json(name = "status")
    val status: NotionStatus? = null,
    val date: NotionDate? = null,
    val rollup: NotionRollup? = null,
    val relation: List<NotionRelation>? = null
)

@JsonClass(generateAdapter = true)
data class NotionTitle(
    val type: String,
    @param:Json(name = "plain_text")
    val plainText: String
)

@JsonClass(generateAdapter = true)
data class NotionStatus(
    val name: String
)

@JsonClass(generateAdapter = true)
data class NotionBodyRequest(
    val filter: FilterRequest,
    val sorts: List<SortRequest>,
    @param:Json(name = "start_cursor")
    val startCursor: String? = null
)

@JsonClass(generateAdapter = true)
class FilterRequest(
    val property: String? = null,
    val status: FilterStatusRequest? = null,
    val date: FilterDateRequest? = null,
    val or: List<FilterRequest>? = null,
    val and: List<FilterRequest>? = null,
    val select: FilterStatusRequest? = null,
    val checkbox: FilterCheckboxRequest? = null,
    val relation: FilterRelationRequest? = null
)

@JsonClass(generateAdapter = true)
class FilterStatusRequest(
    val equals: String? = null,
    @param:Json(name = "does_not_equal") val doesNotEqual: String? = null
)

@JsonClass(generateAdapter = true)
class FilterRelationRequest(
    @param:Json(name = "is_empty") val isEmpty: Boolean? = null
)

@JsonClass(generateAdapter = true)
class FilterDateRequest(
    @param:Json(name = "on_or_before")
    val onOrBefore: String? = null,
    @param:Json(name = "is_not_empty")
    val isNotEmpty: Boolean? = null
)

@JsonClass(generateAdapter = true)
class FilterCheckboxRequest(
    val equals: Boolean
)

@JsonClass(generateAdapter = true)
class SortRequest(
    val property: String? = null,
    val timestamp: String? = null,
    val direction: String
)

@JsonClass(generateAdapter = true)
class NotionDatabaseQueryResult(
    val results: List<NotionPage>,
    @param:Json(name = "next_cursor")
    val nextCursor: String?
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
    @param:Json(name = "multi_select")
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
    @param:Json(name = "database_id") val databaseId: String
)

@JsonClass(generateAdapter = true)
data class AddPageNotionProperty(
    @param:Json(name = "rich_text") val richText: List<AddPageNotionPropertyRichText>? = null,
    val title: List<AddPageNotionPropertyTitle>? = null,
    val url: String? = null,
    val select: AddPageNotionPropertySelect? = null,
    val status: AddPageNotionPropertySelect? = null,
    val date: AddPageNotionPropertyDate? = null,
)

sealed class ApiNotionProperty {
    @JsonClass(generateAdapter = true)
    data class RichText(
        @param:Json(
        name = "rich_text"
    ) val richText: List<AddPageNotionPropertyRichText>
    ) : ApiNotionProperty()

    @JsonClass(generateAdapter = true)
    data class Title(val title: List<AddPageNotionPropertyTitle>) : ApiNotionProperty()

    @JsonClass(generateAdapter = true)
    data class Url(val url: String?) : ApiNotionProperty()

    @JsonClass(generateAdapter = true)
    data class Select(val select: AddPageNotionPropertySelect) : ApiNotionProperty()

    @JsonClass(generateAdapter = true)
    data class Status(val status: AddPageNotionPropertySelect) : ApiNotionProperty()

    @JsonClass(generateAdapter = true)
    data class Date(val date: AddPageNotionPropertyDate) : ApiNotionProperty()
}

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyRichText(
    val text: AddPageNotionPropertyText
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyTitle(
    val text: AddPageNotionPropertyText
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertySelect(
    val name: String
)

@JsonClass(generateAdapter = true)
data class AddPageNotionPropertyDate(
    val start: String
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
    val properties: Map<String, AddPageNotionProperty>,
)

@JsonClass(generateAdapter = true)
class UpdatePropertiesBodyRequest(
    val properties: Map<String, ApiNotionProperty>,
)

@JsonClass(generateAdapter = true)
data class NotionDate(
    val start: String?,
    val end: String?,
    @param:Json(name = "time_zone") val timeZone: String?
)

@JsonClass(generateAdapter = true)
data class NotionRollup(
    val number: Float?
)

@JsonClass(generateAdapter = true)
data class NotionRelation(
    val id: String
)
