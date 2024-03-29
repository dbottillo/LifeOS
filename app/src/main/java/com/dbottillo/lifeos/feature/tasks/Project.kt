package com.dbottillo.lifeos.feature.tasks

data class Project(
    val id: String,
    val text: String,
    val url: String,
    val color: String,
    val due: String,
    val progress: Float?,
    val status: Status,
    val link: String?
)
