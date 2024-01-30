package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterBeforeRequest
import com.dbottillo.lifeos.network.FilterCheckboxRequest
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
                                property = "Category",
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
                                property = "Category",
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
                            ),
                            FilterRequest(
                                property = "Favourite",
                                checkbox = FilterCheckboxRequest(
                                    equals = false
                                )
                            )
                        )
                    )
                )
            ),
            sorts = listOf(
                SortRequest(
                    property = "Favourite",
                    direction = "descending"
                ),
                SortRequest(
                    property = "Status",
                    direction = "ascending"
                ),
                SortRequest(
                    property = "Due",
                    direction = "ascending"
                )
            )
        )
    }
}
