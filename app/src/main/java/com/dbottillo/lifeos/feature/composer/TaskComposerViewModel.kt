package com.dbottillo.lifeos.feature.composer

import android.annotation.SuppressLint
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.tasks.TaskManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaskComposerViewModel @Inject constructor(
    private val articleManager: ArticleManager,
    private val taskManager: TaskManager,
) : ViewModel() {

    val events: Channel<ComposerEvents> = Channel()

    val state = MutableStateFlow(
        ComposerState()
    )

    fun saveArticle() {
        val url = state.value.sanitizedUrl
        articleManager.addArticle(title = state.value.title, url = url)
        events.trySend(ComposerEvents.Finish)
    }

    fun saveLifeOs() {
        taskManager.addTask(
            title = state.value.title,
            url = state.value.sanitizedUrl,
            type = state.value.typeSelection,
            status = state.value.statusSelection,
            due = state.value.selectedDueDate
        )
        events.trySend(ComposerEvents.Finish)
    }

    fun init(url: String?, title: String?) {
        viewModelScope.launch {
            state.value = state.first().copy(
                url = url ?: "",
                title = title ?: ""
            )
        }
    }

    fun onTitleChange(newTitle: String) {
        viewModelScope.launch {
            state.value = state.first().copy(
                title = newTitle
            )
        }
    }

    fun onUrlChange(newUrl: String) {
        viewModelScope.launch {
            state.value = state.first().copy(
                url = newUrl
            )
        }
    }

    fun onTypeSelected(type: String) {
        viewModelScope.launch {
            state.value = state.first().copy(
                typeSelection = type
            )
        }
    }

    fun onStatusSelected(status: String) {
        viewModelScope.launch {
            state.value = state.first().copy(
                statusSelection = status
            )
        }
    }

    fun onSelectDate() {
        viewModelScope.launch {
            state.value = state.first().copy(
                showDueDatePicker = true
            )
        }
    }

    fun onDateSelected(newDate: Long?) {
        viewModelScope.launch {
            state.value = state.first().copy(
                selectedDueDate = newDate,
                showDueDatePicker = false
            )
        }
    }

    fun onDateSelectionDismiss() {
        viewModelScope.launch {
            state.value = state.first().copy(
                showDueDatePicker = false
            )
        }
    }
}

data class ComposerState(
    val title: String = "",
    val url: String = "",
    val typeSelection: String = "None",
    val typeSelectorOptions: List<String> = listOf(
        "None",
        "Idea",
        "Task",
        "Resource",
        "Project",
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
) {
    val sanitizedUrl = url.split("?").first()
    val saveArticleEnabled = Patterns.WEB_URL.matcher(sanitizedUrl).matches() && url.isNotEmpty() &&
            typeSelection != "None" && statusSelection != "None"

    val formattedDate = selectedDueDate?.let { formatter.format(Date(selectedDueDate)) }
}

@SuppressLint("ConstantLocale")
val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

sealed class ComposerEvents {
    data object Finish : ComposerEvents()
}
