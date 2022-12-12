package com.dbottillo.notionalert.feature.home

import kotlinx.coroutines.flow.Flow

interface PocketStorage {
    suspend fun updateOauthCode(code: String)
    suspend fun updateAuthorizationCode(code: String)
    suspend fun updateNumberToRead(total: Int)

    val oauthCodeFlow: Flow<String>
    val authorizationCodeFlow: Flow<String>
    val numberToReadFlow: Flow<Int>
}
