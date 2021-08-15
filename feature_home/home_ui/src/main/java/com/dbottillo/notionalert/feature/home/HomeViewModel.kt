package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.NotionPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val flow: MutableStateFlow<ApiResult<NotionPage>?> = MutableStateFlow(null)

    val state: StateFlow<ApiResult<NotionPage>?> = flow

    fun load() {
        viewModelScope.launch {
            val response = repository.makeNetworkRequest()
            flow.emit(response)
        }
    }
}
