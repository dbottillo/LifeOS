package com.dbottillo.lifeos.sharing

import androidx.lifecycle.ViewModel
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.tasks.TaskManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val articleManager: ArticleManager,
    private val taskManager: TaskManager,
) : ViewModel() {

    val events: Channel<Boolean> = Channel()

    fun saveArticle(url: String, title: String?) {
        articleManager.addArticle(title = title, url = url)
        events.trySend(true)
    }

    fun saveLifeOs(url: String, title: String?) {
        taskManager.addTask(title = title, url = url)
        events.trySend(true)
    }
}
