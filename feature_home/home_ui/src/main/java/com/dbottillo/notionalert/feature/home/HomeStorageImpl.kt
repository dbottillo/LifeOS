package com.dbottillo.notionalert.feature.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Home")

class HomeStorageImpl constructor(
    private val context: Context
) : HomeStorage {

    override val timestamp: Flow<OffsetDateTime> = context.dataStore.data
        .map { preferences ->
            val value = preferences[TIMESTAMP] ?: ""
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }

    override val data: Flow<StorageInfo> = context.dataStore.data
        .map { preferences ->
            val value = preferences[TIMESTAMP] ?: ""
            val timestamp = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            StorageInfo(
                nextActions = preferences[NEXT_ACTIONS] ?: "Loading...",
                mainPage = preferences[MAIN_PAGE] ?: "Loading...",
                timeStamp = timestamp
            )
        }

    override suspend fun updateTimestamp() {
        val now = OffsetDateTime.now()
        val timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)
        context.dataStore.edit { settings ->
            settings[TIMESTAMP] = timestamp
        }
    }

    override suspend fun saveMainPage(data: String) {
        context.dataStore.edit { settings ->
            settings[MAIN_PAGE] = data
        }
    }

    override suspend fun saveNextActions(data: String) {
        context.dataStore.edit { settings ->
            settings[NEXT_ACTIONS] = data
        }
    }
}

private val TIMESTAMP = stringPreferencesKey("timestamp")
private val NEXT_ACTIONS = stringPreferencesKey("nextActions")
private val MAIN_PAGE = stringPreferencesKey("mainPage")
