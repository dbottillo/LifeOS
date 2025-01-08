package com.dbottillo.lifeos.feature.tasks

import java.util.Date

data class Area(
    val id: String,
    val text: String,
    val url: String,
    val link: String?
)

data class Resource(
    val id: String,
    val text: String,
    val url: String,
    val link: String?,
    val parent: Parent?
)

data class Idea(
    val id: String,
    val text: String,
    val url: String,
    val link: String?,
    val parent: Parent?
)

data class Project(
    val id: String,
    val text: String,
    val url: String,
    val color: String,
    val due: Date?,
    val dueFormatted: String?,
    val progress: Float?,
    val status: Status,
    val link: String?,
    val parent: Parent?
)

data class NextAction(
    val id: String,
    val text: String,
    val url: String,
    val color: String,
    val due: Date?,
    val dueFormatted: String?,
    val link: String?,
    val isInbox: Boolean,
    val parent: Parent?
)

data class Blocked(
    val id: String,
    val text: String,
    val url: String,
    val color: String,
    val due: Date?,
    val dueFormatted: String?,
    val link: String?,
    val parent: Parent?
)

data class Parent(
    val id: String,
    val title: String
)

data class Goal(
    val id: String,
    val text: String,
    val url: String,
    val color: String,
    val parent: Parent?,
    val status: Status,
)
