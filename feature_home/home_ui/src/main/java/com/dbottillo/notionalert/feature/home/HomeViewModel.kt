package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.NotificationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val notificationProvider: NotificationProvider
) : ViewModel() {

    val state: StateFlow<AppState> = repository.state

    fun load() {
        viewModelScope.launch {
            repository.makeNetworkRequest()
        }
    }

    fun removeNotification() {
        notificationProvider.clear()
    }
}
