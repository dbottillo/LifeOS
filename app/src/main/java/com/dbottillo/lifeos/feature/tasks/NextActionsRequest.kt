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
                        and = listOf(
                            FilterRequest(
                                property = "Due",
                                date = FilterBeforeRequest(onOrBefore = date)
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Task"
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
                    FilterRequest(
                        and = listOf(
                            FilterRequest(
                                property = "Status",
                                status = FilterEqualsRequest(
                                    equals = "Inbox"
                                )
                            )
                        )
                    )
                )
            ),
            sorts = listOf(
                SortRequest(
                    property = "Due",
                    direction = "ascending"
                ),
                SortRequest(
                    property = "Parent item",
                    direction = "ascending"
                )
            )
        )
    }
}
