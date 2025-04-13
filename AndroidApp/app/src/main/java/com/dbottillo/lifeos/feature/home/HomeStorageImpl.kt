package com.dbottillo.lifeos.feature.home

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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Home")

class HomeStorageImpl(
    private val context: Context
) : HomeStorage {

    override val timestamp: Flow<OffsetDateTime> = context.dataStore.data
        .map { preferences ->
            preferences[TIMESTAMP]?.let {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } ?: OffsetDateTime.now()
        }

    override suspend fun updateTimestamp() {
        val now = OffsetDateTime.now()
        val timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)
        context.dataStore.edit { settings ->
            settings[TIMESTAMP] = timestamp
        }
    }
}

private val TIMESTAMP = stringPreferencesKey("timestamp")
