package com.dbottillo.lifeos.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class NotionDatabaseBlockResult(
    val results: List<NotionDatabaseBlock>
)

@JsonClass(generateAdapter = true)
class NotionDatabaseBlock(
    val id: String,
    val type: String,
    val paragraph: NotionDatabaseBlockParagraph? = null,
    @Json(name = "numbered_list_item") val numberedListItem: NotionDatabaseBlockNumberedListItem? = null
)

@JsonClass(generateAdapter = true)
class NotionDatabaseBlockParagraph(
    val text: List<NotionDatabaseBlockText>
)

@JsonClass(generateAdapter = true)
class NotionDatabaseBlockNumberedListItem(
    val text: List<NotionDatabaseBlockText>
)

@JsonClass(generateAdapter = true)
class NotionDatabaseBlockText(
    @Json(name = "plain_text") val plainText: String
)
