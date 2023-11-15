package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.RefreshProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider
) : ViewModel() {

    val state: StateFlow<AppState> = repository.state

    fun load() {
        viewModelScope.launch {
            repository.makeNetworkRequest()
            repository.fetchArticles()
            refreshProvider.start()
        }
    }

    fun removeNotification() {
        notificationProvider.clear()
        refreshProvider.stop()
    }
}
