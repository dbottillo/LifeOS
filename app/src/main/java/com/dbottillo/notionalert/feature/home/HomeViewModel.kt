package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.NotificationProvider
import com.dbottillo.notionalert.RefreshProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val notificationProvider: NotificationProvider,
    private val refreshProvider: RefreshProvider,
    private val pocketStorage: PocketStorage
) : ViewModel() {

    val state: StateFlow<AppState> = repository.state

    val eventChannel = MutableSharedFlow<PocketEvents>()

    private val _pocketFlow = pocketStorage.authorizationCodeFlow.map { authorization ->
        when {
            authorization.isNotEmpty() -> {
                PocketState.Authorized(accessToken = authorization, userName = "whatever")
            }
            else -> {
                PocketState.Idle
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PocketState.Idle)

    val pocketState = _pocketFlow

    fun load() {
        viewModelScope.launch {
            repository.makeNetworkRequest()
            repository.fetchPocketArticles()
            refreshProvider.start()
        }
    }

    fun removeNotification() {
        notificationProvider.clear()
        refreshProvider.stop()
    }

    fun connectToPocket() {
        viewModelScope.launch {
            when (val result = repository.getPocketToken()) {
                is ApiResult.Error -> eventChannel.emit(PocketEvents.Error(result.exception))
                is ApiResult.Success -> {
                    pocketStorage.updateOauthCode(result.data)
                    eventChannel.emit(PocketEvents.OpenPocket(result.data))
                }
            }
        }
    }

    fun tryToAuthorizePocket() {
        viewModelScope.launch {
            val oauthCode = pocketStorage.oauthCodeFlow.first()
            if (oauthCode.isEmpty()) {
                eventChannel.emit(PocketEvents.Error(Throwable("no oauth code to authorize")))
            } else {
                authorizePocket(oauthCode)
            }
        }
    }

    private fun authorizePocket(code: String) {
        viewModelScope.launch {
            when (val result = repository.authorizePocket(code)) {
                is ApiResult.Error -> {
                    pocketStorage.updateOauthCode("")
                    eventChannel.emit(PocketEvents.Error(result.exception))
                }
                is ApiResult.Success -> {
                    pocketStorage.updateAuthorizationCode(result.data.first)
                }
            }
        }
    }

    fun immediateRefresh() {
        refreshProvider.immediate()
    }
}

sealed class PocketState {
    object Idle : PocketState()
    data class Authorized(val accessToken: String, val userName: String) : PocketState()
}

sealed class PocketEvents {
    data class Error(val throwable: Throwable) : PocketEvents()
    data class OpenPocket(val code: String) : PocketEvents()
}
