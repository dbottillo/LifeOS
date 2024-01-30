package com.dbottillo.lifeos.feature.tasks

sealed class Status {
    data object Inbox : Status()
    data object Idea : Status()
    data object Focus : Status()
    data object Backlog : Status()
    data object WaitingFor : Status()
    data object Recurring : Status()
    data object None : Status()
    data object Archive : Status()
    data object Done : Status()
    data object Unknown : Status()
}

fun String.toStatus(): Status {
    return when (this) {
        "Inbox" -> Status.Inbox
        "Idea" -> Status.Idea
        "Focus" -> Status.Focus
        "Backlog" -> Status.Backlog
        "Waiting For" -> Status.WaitingFor
        "Recurring" -> Status.Recurring
        "none" -> Status.None
        "Archive" -> Status.Archive
        "Done" -> Status.Done
        else -> Status.Unknown
    }
}
