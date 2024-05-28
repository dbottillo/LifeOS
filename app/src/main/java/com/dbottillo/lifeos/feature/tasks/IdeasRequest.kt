package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class IdeasRequest {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
                    FilterRequest(
                        property = "Type",
                        select = FilterEqualsRequest(
                            equals = "Idea"
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
