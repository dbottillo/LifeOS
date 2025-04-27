package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterDateRequest
import com.dbottillo.lifeos.network.FilterStatusRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class FocusInboxSoonRequest(private val date: String) {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
                    FilterRequest(
                        and = listOf(
                            FilterRequest(
                                property = "Due",
                                date = FilterDateRequest(onOrBefore = date)
                            ),
                            FilterRequest(
                                property = "Status",
                                status = FilterStatusRequest(
                                    equals = "Recurring"
                                )
                            )
                        )
                    ),
                    FilterRequest(
                        and = listOf(
                            FilterRequest(
                                property = "Type",
                                select = FilterStatusRequest(
                                    equals = "Task"
                                )
                            ),
                            FilterRequest(
                                property = "Status",
                                status = FilterStatusRequest(
                                    doesNotEqual = "Recurring"
                                )
                            ),
                            FilterRequest(
                                property = "Due",
                                date = FilterDateRequest(
                                    isNotEmpty = true
                                )
                            )
                        ),
                    ),
                    FilterRequest(
                        or = listOf(
                            FilterRequest(
                                property = "Status",
                                status = FilterStatusRequest(
                                    equals = "Inbox"
                                )
                            ),
                            FilterRequest(
                                property = "Status",
                                status = FilterStatusRequest(
                                    equals = "Focus"
                                )
                            )
                        )
                    ),
                )
            ),
            sorts = listOf(
                SortRequest(
                    timestamp = "created_time",
                    direction = "descending"
                )
            )
        )
    }
}
