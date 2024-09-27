package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class IdeasAndOngoingRequest {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
                    FilterRequest(
                        and = listOf(
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Idea"
                                )
                            ),
                            FilterRequest(
                                property = "Status",
                                status = FilterEqualsRequest(doesNotEqual = "Archive")
                            )
                        ),
                    ),
                    FilterRequest(
                        property = "Status",
                        status = FilterEqualsRequest(
                            equals = "Ongoing"
                        )
                    )
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
