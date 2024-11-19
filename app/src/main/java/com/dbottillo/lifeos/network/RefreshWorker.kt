package com.dbottillo.lifeos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dbottillo.lifeos.feature.articles.ArticleRepository
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.feature.widgets.WidgetsRefresher
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val articleRepository: ArticleRepository,
    private val widgetsRefresher: WidgetsRefresher
) : CoroutineWorker(appContext, workerParams) {

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val actions = async { tasksRepository.loadNextActions() }
            val projectsAreaResourcesAndIdeas = async { tasksRepository.loadProjectsAreaResourcesAndIdeas() }
            val articles = async { articleRepository.fetchArticles() }
            awaitAll(actions, articles, projectsAreaResourcesAndIdeas)
            widgetsRefresher.refreshAll()
            return@withContext Result.success()
        } catch (error: Throwable) {
            Firebase.crashlytics.recordException(error)
            return@withContext Result.failure()
        }
    }
}
