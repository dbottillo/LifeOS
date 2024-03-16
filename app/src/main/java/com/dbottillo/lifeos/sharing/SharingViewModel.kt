package com.dbottillo.lifeos.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.ArticleManager
import com.dbottillo.lifeos.feature.logs.LogLevel
import com.dbottillo.lifeos.feature.logs.LogTags
import com.dbottillo.lifeos.feature.logs.LogsRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val articleManager: ArticleManager,
    private val logsRepository: LogsRepository
) : ViewModel() {

    val events: Channel<Boolean> = Channel()

    fun saveArticle(url: String, title: String?) {
        articleManager.addArticle(title = title, url = url)
        events.trySend(true)
    }

    fun saveLifeOs(url: String, title: String?) {
        viewModelScope.launch {
            logsRepository.addEntry(LogTags.SHARE, level = LogLevel.INFO, "Saving life os entry for $url url")
            when (val res = tasksRepository.addTask(AppConstant.GTD_ONE_DATABASE_ID, title, url)) {
                is ApiResult.Error -> logsRepository.addEntry(
                    LogTags.SHARE,
                    level = LogLevel.ERROR,
                    "Error Saving life os entry, message: ${res.exception}"
                )
                is ApiResult.Success -> logsRepository.addEntry(
                    LogTags.SHARE,
                    level = LogLevel.INFO,
                    "Saving life os entry, ok"
                )
            }
            events.send(true)
        }
    }
}
