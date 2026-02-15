package com.dbottillo.lifeos.feature.composer

import com.dbottillo.lifeos.db.NotionEntryWithParent
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalNotionParentsUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {
    operator fun invoke(query: String): Flow<List<NotionEntryWithParent>> {
        return tasksRepository.searchParents(query)
    }
}
