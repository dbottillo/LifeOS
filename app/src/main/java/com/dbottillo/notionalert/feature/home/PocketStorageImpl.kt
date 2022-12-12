package com.dbottillo.notionalert.feature.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pocketDataStore: DataStore<Preferences> by preferencesDataStore(name = "Pocket")

class PocketStorageImpl constructor(
    private val context: Context
) : PocketStorage {

    override suspend fun updateOauthCode(code: String) {
        context.pocketDataStore.edit { settings ->
            settings[OAUTH_CODE] = code
        }
    }

    override suspend fun updateAuthorizationCode(code: String) {
        context.pocketDataStore.edit { settings ->
            settings[AUTHORIZATION_CODE] = code
        }
    }

    override val oauthCodeFlow: Flow<String> = context.pocketDataStore.data
        .map { preferences ->
            preferences[OAUTH_CODE] ?: ""
        }

    override val authorizationCodeFlow: Flow<String> = context.pocketDataStore.data
        .map { preferences ->
            preferences[AUTHORIZATION_CODE] ?: ""
        }

    override suspend fun updateNumberToRead(total: Int) {
        context.pocketDataStore.edit { settings ->
            settings[NUMBER_TO_READ] = total
        }
    }

    override val numberToReadFlow: Flow<Int> = context.pocketDataStore.data
        .map { preferences ->
            preferences[NUMBER_TO_READ] ?: -1
        }
}

private val OAUTH_CODE = stringPreferencesKey("pocket_oauth_code")
private val AUTHORIZATION_CODE = stringPreferencesKey("pocket_authorization_code")
private val NUMBER_TO_READ = intPreferencesKey("pocket_number_to_read")
