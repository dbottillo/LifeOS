package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterBeforeRequest
import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class NextActionsRequest(private val date: String) {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
                    FilterRequest(
                        or = listOf(
                            FilterRequest(
                                property = "Due",
                                date = FilterBeforeRequest(onOrBefore = date)
                            ),
                            FilterRequest(
                                property = "Status",
                                status = FilterEqualsRequest(
                                    equals = "Inbox"
                                )
                            )
                        )
                    ),
                    FilterRequest(
                        and = listOf(
                            FilterRequest(
                                property = "Status",
                                status = FilterEqualsRequest(
                                    equals = "Focus"
                                )
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Task"
                                )
                            )
                        )
                    ),
                )
            ),
            sorts = listOf(
                SortRequest(
                    property = "Status",
                    direction = "ascending"
                ),
            )
        )
    }
}
