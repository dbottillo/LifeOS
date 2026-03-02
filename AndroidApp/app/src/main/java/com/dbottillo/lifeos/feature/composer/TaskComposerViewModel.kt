package com.dbottillo.lifeos.feature.composer

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.tasks.NotionEntryDateMapper
import com.dbottillo.lifeos.feature.tasks.TaskManager
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import com.dbottillo.lifeos.db.NotionEntryWithParent
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaskComposerViewModel @Inject constructor(
    private val articleManager: ArticleManager,
    private val taskManager: TaskManager,
    private val tasksRepository: TasksRepository,
    private val notionEntryDateMapper: NotionEntryDateMapper,
    private val getLocalNotionParentsUseCase: GetLocalNotionParentsUseCase
) : ViewModel() {

    val state: MutableStateFlow<ComposerState> = MutableStateFlow(ComposerState.Loading)

    fun init(input: ComposerInput) {
        if (input.entryId.isNullOrEmpty()) {
            viewModelScope.launch {
                state.value = ComposerState.Data(
                    entryId = null,
                    title = input.title ?: "",
                    link = input.url ?: "",
                )
            }
        } else {
            viewModelScope.launch {
                state.value = ComposerState.Loading
                // load from db
                val entry = tasksRepository.loadTask(input.entryId)
                val date = notionEntryDateMapper.map(entry)?.first
                val parentEntry = entry.parentId?.let { tasksRepository.loadTask(it) }

                var selectedDueTime: Pair<Int, Int>? = null
                var selectedDueDate: Long? = date?.time
                if (date != null && entry.startDate?.contains("T") == true) {
                    val localCalendar = java.util.Calendar.getInstance()
                    localCalendar.time = date
                    selectedDueTime = localCalendar.get(java.util.Calendar.HOUR_OF_DAY) to localCalendar.get(java.util.Calendar.MINUTE)

                    val utcCalendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                    utcCalendar.set(
                        localCalendar.get(java.util.Calendar.YEAR),
                        localCalendar.get(java.util.Calendar.MONTH),
                        localCalendar.get(java.util.Calendar.DAY_OF_MONTH),
                        0,
                        0,
                        0
                    )
                    utcCalendar.set(java.util.Calendar.MILLISECOND, 0)
                    selectedDueDate = utcCalendar.timeInMillis
                }

                state.value = ComposerState.Data(
                    entryId = input.entryId,
                    title = entry.title ?: "",
                    link = entry.link ?: "",
                    selectedDueDate = selectedDueDate,
                    selectedDueTime = selectedDueTime,
                    statusSelection = entry.status,
                    typeSelection = entry.type,
                    selectedParentId = parentEntry?.uid,
                    selectedParentTitle = parentEntry?.title
                )
            }
        }
    }

    val events: Channel<ComposerEvents> = Channel()

    private val _parentSearchQuery = MutableStateFlow("")
    private val _parentSearchResults = MutableStateFlow<List<NotionEntryWithParent>>(emptyList())

    init {
        viewModelScope.launch {
            _parentSearchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    getLocalNotionParentsUseCase(query)
                }
                .collect { results ->
                    _parentSearchResults.value = results
                    state.value = dataState().copy(
                        parentSearchResults = results
                    )
                }
        }
    }

    fun saveArticle() {
        val value = (state.value as ComposerState.Data)
        val url = value.sanitizedUrl
        articleManager.addArticle(title = value.title, url = url)
        events.trySend(ComposerEvents.Finish)
    }

    fun saveLifeOs() {
        val value = (state.value as ComposerState.Data)
        if (value.editTaskMode) {
            editTask()
        } else {
            taskManager.addTask(
                title = value.title,
                url = value.sanitizedUrl,
                type = value.typeSelection,
                status = value.statusSelection,
                due = getCombinedDueTimestamp(value.selectedDueDate, value.selectedDueTime),
                hasTime = value.selectedDueTime != null,
                parentId = value.selectedParentId
            )
            events.trySend(ComposerEvents.Finish)
        }
    }

    private fun editTask() {
        viewModelScope.launch {
            val value = (state.value as ComposerState.Data)
            state.value = dataState().copy(
                editingInProgress = true
            )
            val result = tasksRepository.editTask(
                entryId = value.entryId!!,
                title = value.title,
                link = value.sanitizedUrl,
                type = value.typeSelection,
                status = value.statusSelection,
                due = getCombinedDueTimestamp(value.selectedDueDate, value.selectedDueTime),
                hasTime = value.selectedDueTime != null,
                parentId = value.selectedParentId
            )
            if (result is ApiResult.Success) {
                events.send(ComposerEvents.Finish)
                return@launch
            }
            events.send(ComposerEvents.Error(result.toString()))
            state.value = dataState().copy(
                editingInProgress = false
            )
        }
    }

    private fun getCombinedDueTimestamp(date: Long?, time: Pair<Int, Int>?): Long? {
        if (date == null) return null
        if (time == null) return date
        val utcCalendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = date
        val localCalendar = java.util.Calendar.getInstance()
        localCalendar.set(
            utcCalendar.get(java.util.Calendar.YEAR),
            utcCalendar.get(java.util.Calendar.MONTH),
            utcCalendar.get(java.util.Calendar.DAY_OF_MONTH),
            time.first,
            time.second
        )
        localCalendar.set(java.util.Calendar.SECOND, 0)
        localCalendar.set(java.util.Calendar.MILLISECOND, 0)
        return localCalendar.timeInMillis
    }

    fun onTitleChange(newTitle: String) {
        viewModelScope.launch {
            state.value = dataState().copy(
                title = newTitle
            )
        }
    }

    fun onUrlChange(newUrl: String) {
        viewModelScope.launch {
            state.value = dataState().copy(
                link = newUrl
            )
        }
    }

    fun onTypeSelected(type: String) {
        viewModelScope.launch {
            state.value = dataState().copy(
                typeSelection = type
            )
        }
    }

    fun onStatusSelected(status: String) {
        viewModelScope.launch {
            state.value = dataState().copy(
                statusSelection = status
            )
        }
    }

    fun onSelectDate() {
        viewModelScope.launch {
            state.value = dataState().copy(
                showDueDatePicker = true
            )
        }
    }

    fun onDateSelected(newDate: Long?) {
        viewModelScope.launch {
            state.value = dataState().copy(
                selectedDueDate = newDate,
                selectedDueTime = null,
                showDueDatePicker = false
            )
        }
    }

    fun onDateAndTimeSelected(newDate: Long?, hour: Int, minute: Int) {
        viewModelScope.launch {
            state.value = dataState().copy(
                selectedDueDate = newDate,
                selectedDueTime = hour to minute,
                showDueDatePicker = false
            )
        }
    }

    fun onClearTime() {
        viewModelScope.launch {
            state.value = dataState().copy(
                selectedDueTime = null
            )
        }
    }

    fun onDateSelectionDismiss() {
        viewModelScope.launch {
            state.value = dataState().copy(
                showDueDatePicker = false
            )
        }
    }

    fun onParentSelected(notionEntryWithParent: NotionEntryWithParent) {
        viewModelScope.launch {
            state.value = dataState().copy(
                selectedParentId = notionEntryWithParent.notionEntry.uid,
                selectedParentTitle = notionEntryWithParent.notionEntry.title,
                parentSearchQuery = ""
            )
        }
    }

    fun onParentSearchQueryChanged(query: String) {
        _parentSearchQuery.value = query
        viewModelScope.launch {
            state.value = dataState().copy(
                parentSearchQuery = query,
                parentSearchResults = _parentSearchResults.value // update the state directly
            )
        }
    }

    fun onClearParentSelected() {
        viewModelScope.launch {
            state.value = dataState().copy(
                selectedParentId = null,
                selectedParentTitle = null
            )
        }
    }

    private fun dataState(): ComposerState.Data {
        return state.value as? ComposerState.Data
            ?: throw IllegalStateException("State is not Data: ${state.value}")
    }
}

@Serializable
data class ComposerInput(
    val entryId: String? = null,
    val title: String? = null,
    val url: String? = null
)
sealed class ComposerState {
    data object Loading : ComposerState()
    data class Data(
        val entryId: String?,
        val title: String,
        val link: String,
        val typeSelection: String = "Task",
        val typeSelectorOptions: List<String> = listOf(
            "Task",
            "Resource",
            "Folder",
            "Bookmark",
            "Area"
        ),
        val statusSelection: String = "Backlog",
        val statusSelectorOptions: List<String> = listOf(
            "Focus",
            "Backlog",
            "Recurring",
            "Archive",
            "Done"
        ),

        val selectedParentId: String? = null,
        val selectedParentTitle: String? = null,
        val parentSearchQuery: String = "",
        val parentSearchResults: List<NotionEntryWithParent> = emptyList(),
        val showDueDatePicker: Boolean = false,
        val selectedDueDate: Long? = null,
        val selectedDueTime: Pair<Int, Int>? = null,
        val editingInProgress: Boolean = false
    ) : ComposerState() {
        val sanitizedUrl = link.split("?").first()
        val saveArticleEnabled = sanitizedUrl.isNotEmpty() && selectedDueDate == null
        val formattedDate = selectedDueDate?.let {
            if (selectedDueTime != null) {
                val utcCalendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                utcCalendar.timeInMillis = selectedDueDate
                val localCalendar = java.util.Calendar.getInstance()
                localCalendar.set(
                    utcCalendar.get(java.util.Calendar.YEAR),
                    utcCalendar.get(java.util.Calendar.MONTH),
                    utcCalendar.get(java.util.Calendar.DAY_OF_MONTH),
                    selectedDueTime.first,
                    selectedDueTime.second
                )
                formatterWithTime.format(localCalendar.time)
            } else {
                formatter.format(Date(selectedDueDate))
            }
        }
        val showArticle: Boolean = entryId == null
        val editTaskMode = entryId != null
    }
}

@SuppressLint("ConstantLocale")
val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@SuppressLint("ConstantLocale")
val formatterWithTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

sealed class ComposerEvents {
    data object Finish : ComposerEvents()
    data class Error(val message: String) : ComposerEvents()
}
