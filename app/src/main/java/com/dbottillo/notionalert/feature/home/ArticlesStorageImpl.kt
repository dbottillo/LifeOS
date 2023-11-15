package com.dbottillo.notionalert.feature.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.articlesDataStore: DataStore<Preferences> by preferencesDataStore(name = "Articles")

class ArticlesStorageImpl constructor(
    private val context: Context
) : ArticlesStorage {

    override suspend fun updateNumberToRead(total: Int) {
        context.articlesDataStore.edit { settings ->
            settings[numberToRead] = total
        }
    }

    override val numberToReadFlow: Flow<Int> = context.articlesDataStore.data
        .map { preferences ->
            preferences[numberToRead] ?: -1
        }
}

private val numberToRead = intPreferencesKey("articles_number_to_read")
