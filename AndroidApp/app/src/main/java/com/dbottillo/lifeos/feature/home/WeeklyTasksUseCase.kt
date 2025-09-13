package com.dbottillo.lifeos.feature.home

import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.ui.EntryContent
import com.dbottillo.lifeos.ui.mapSoon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.groupBy

class WeeklyTasksUseCase @Inject constructor(
    tasksRepository: TasksRepository,
) {

    private val calendar = Calendar.getInstance()

    val flow: Flow<Map<String, List<EntryContent>>> = tasksRepository.soonFlow.map { soon ->
        soon.groupBy { s ->
            s.due?.let {
                calendar.time = it
                SimpleDateFormat("EEEE dd/MM", Locale.getDefault()).format(calendar.time)
            } ?: "No date"
        }.mapValues {
            it.value.mapSoon()
        }
    }
}
