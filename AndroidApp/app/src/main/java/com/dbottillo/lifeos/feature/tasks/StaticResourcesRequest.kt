package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterStatusRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class StaticResourcesRequest {

    fun get(resources: List<String>): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                and = listOf(
                    FilterRequest(
                        or = resources.map { res ->
                            FilterRequest(
                                property = "Type",
                                select = FilterStatusRequest(
                                    equals = res
                                )
                            )
                        }
                    ),
                    FilterRequest(
                        property = "Status",
                        status = FilterStatusRequest(doesNotEqual = "Archive")
                    )
                )
            ),
            sorts = listOf(
                SortRequest(
                    timestamp = "last_edited_time",
                    direction = "descending"
                )
            )
        )
    }
}
