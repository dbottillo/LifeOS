package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class StaticResourcesRequest {

    fun get(resources: List<String>): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                and = listOf(
                    FilterRequest(
                        /*or = listOf(
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Area"
                                )
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Project"
                                )
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Resource"
                                )
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Goal"
                                )
                            ),
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = "Idea"
                                )
                            )
                        )*/
                        or = resources.map { res ->
                            FilterRequest(
                                property = "Type",
                                select = FilterEqualsRequest(
                                    equals = res
                                )
                            )
                        }
                    ),
                    FilterRequest(
                        property = "Status",
                        status = FilterEqualsRequest(doesNotEqual = "Archive")
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
