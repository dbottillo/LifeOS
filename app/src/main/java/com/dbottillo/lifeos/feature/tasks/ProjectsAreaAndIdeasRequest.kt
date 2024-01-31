package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest

class ProjectsAreaAndIdeasRequest {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                or = listOf(
                    FilterRequest(
                        property = "Category",
                        select = FilterEqualsRequest(
                            equals = "Area"
                        )
                    ),
                    FilterRequest(
                        property = "Category",
                        select = FilterEqualsRequest(
                            equals = "Project"
                        )
                    ),
                    FilterRequest(
                        property = "Status",
                        status = FilterEqualsRequest(
                            equals = "Idea"
                        )
                    )
                )
            ),
            sorts = emptyList()
        )
    }
}
