package com.dbottillo.lifeos.feature.composer

import android.util.Patterns
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.home.EntryContent
import com.dbottillo.lifeos.feature.home.HomeStateBottom
import com.dbottillo.lifeos.feature.tasks.TaskManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
        val url = state.value.sanitizedUrl ?: return
        articleManager.addArticle(title = state.value.title, url = url)
        events.trySend(ComposerEvents.Finish)
    }

    fun saveLifeOs() {
        taskManager.addTask(title = state.value.title, url = state.value.sanitizedUrl)
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
}

data class ComposerState(
    val title: String = "",
    val url: String = ""
) {
    val sanitizedUrl = url.split("?").first()
    val validUrl = Patterns.WEB_URL.matcher(sanitizedUrl).matches()
}


sealed class ComposerEvents{
    data object Finish: ComposerEvents()
}