package com.dbottillo.lifeos.feature.tasks

import com.dbottillo.lifeos.network.FilterEqualsRequest
import com.dbottillo.lifeos.network.FilterRequest
import com.dbottillo.lifeos.network.NotionBodyRequest

class ProjectsRequest {

    fun get(): NotionBodyRequest {
        return NotionBodyRequest(
            filter = FilterRequest(
                property = "Category",
                select = FilterEqualsRequest(
                    equals = "Project"
                )
            ),
            sorts = emptyList()
        )
    }
}
