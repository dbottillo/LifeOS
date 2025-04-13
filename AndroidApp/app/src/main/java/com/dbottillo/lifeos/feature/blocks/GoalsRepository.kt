package com.dbottillo.lifeos.feature.blocks

import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.NotionEntryWithParent
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.toParent
import com.dbottillo.lifeos.feature.tasks.toStatus
import com.dbottillo.lifeos.feature.tasks.toTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalsRepository @Inject constructor(
    private val db: AppDatabase,
    private val mapper: GoalsMapper
) {

    private val dao by lazy { db.notionEntryDao() }
    val goalsFlow: Flow<List<Goal>> = dao.getGoals().map(mapper::mapGoals)
}

class GoalsMapper @Inject constructor() {

    fun mapGoals(input: List<NotionEntryWithParent>): List<Goal> {
        return input.map { entry ->
            Goal(
                id = entry.notionEntry.uid,
                text = entry.notionEntry.toTitle(),
                url = entry.notionEntry.url,
                color = entry.notionEntry.color ?: "",
                parent = entry.parent.toParent(),
                status = entry.notionEntry.status.toStatus(),
            )
        }
    }
}
