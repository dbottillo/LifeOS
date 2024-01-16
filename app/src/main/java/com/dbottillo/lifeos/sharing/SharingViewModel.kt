package com.dbottillo.lifeos.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val articleManager: ArticleManager
) : ViewModel() {

    val events: Channel<Boolean> = Channel()

    fun saveArticle(url: String, title: String?) {
        articleManager.addArticle(title = title, url = url)
        events.trySend(true)
    }

    fun saveLifeOs(url: String, title: String?) {
        viewModelScope.launch {
            tasksRepository.addTask(AppConstant.GTD_ONE_DATABASE_ID, title, url)
            events.send(true)
        }
    }
}
