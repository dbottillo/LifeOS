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
import kotlinx.coroutines.flow.first
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
    private val notionEntryDateMapper: NotionEntryDateMapper
) : ViewModel() {

    val state: MutableStateFlow<ComposerState> = MutableStateFlow(ComposerState.Loading)

    fun init(input: ComposerInput) {
        if (input.entryId.isNullOrEmpty()) {
            viewModelScope.launch {
                state.value = ComposerState.Data(
                    entryId = null,
                    title = input.title ?: "",
                    link = input.url ?: ""
                )
            }
        } else {
            viewModelScope.launch {
                state.value = ComposerState.Loading
                // load from db
                val entry = tasksRepository.loadTask(input.entryId)
                val date = notionEntryDateMapper.map(entry)?.first
                entry.startDate
                state.value = ComposerState.Data(
                    entryId = input.entryId,
                    title = entry.title ?: "",
                    link = entry.link ?: "",
                    selectedDueDate = date?.time,
                    statusSelection = entry.status,
                    typeSelection = entry.type
                )
            }
        }
    }

    val events: Channel<ComposerEvents> = Channel()

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
                due = value.selectedDueDate
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
                due = value.selectedDueDate
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
                showDueDatePicker = false
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

    private suspend fun dataState(): ComposerState.Data {
        return state.first() as ComposerState.Data
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
        val typeSelection: String = "None",
        val typeSelectorOptions: List<String> = listOf(
            "None",
            "Idea",
            "Task",
            "Resource",
            "Folder",
            "Bookmark",
            "Area"
        ),
        val statusSelection: String = "None",
        val statusSelectorOptions: List<String> = listOf(
            "None",
            "Focus",
            "Blocked",
            "Backlog",
            "Recurring",
            "Archive",
            "Done"
        ),
        val showDueDatePicker: Boolean = false,
        val selectedDueDate: Long? = null,
        val editingInProgress: Boolean = false
    ) : ComposerState() {
        val sanitizedUrl = link.split("?").first()
        val saveArticleEnabled =
            sanitizedUrl.isNotEmpty() && typeSelection == "None" && statusSelection == "None" && selectedDueDate == null

        val formattedDate = selectedDueDate?.let { formatter.format(Date(selectedDueDate)) }

        val showArticle: Boolean = entryId == null

        val editTaskMode = entryId != null
    }
}

@SuppressLint("ConstantLocale")
val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

sealed class ComposerEvents {
    data object Finish : ComposerEvents()
    data class Error(val message: String) : ComposerEvents()
}
