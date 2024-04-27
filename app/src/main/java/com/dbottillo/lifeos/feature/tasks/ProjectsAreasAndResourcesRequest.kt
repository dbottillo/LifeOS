package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest
import com.dbottillo.lifeos.network.SortRequest

class ProjectsAreasAndResourcesRequest {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
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
                    )
                )
            ),
            sorts = listOf(
                SortRequest(
                    property = "Favourite",
                    direction = "descending"
                ),
                SortRequest(
                    timestamp = "last_edited_time",
                    direction = "descending"
                )
            )
        )
    }
}
