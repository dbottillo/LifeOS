package com.dbottillo.notionalert.feature.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dbottillo.notionalert.data.NextAction
import com.dbottillo.notionalert.data.NextActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Home")

class HomeStorageImpl constructor(
    private val context: Context
) : HomeStorage {

    override val timestamp: Flow<OffsetDateTime> = context.dataStore.data
        .map { preferences ->
            preferences[TIMESTAMP]?.let {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } ?: OffsetDateTime.now()
        }

    override val nextActionsFlow: Flow<NextActions> = context.dataStore.data
        .map { preferences ->
            preferences[NEXT_ACTIONS]?.let {
                val list = mutableListOf<NextAction>()
                it.split("***").forEach {
                    val split = it.split("###")
                    list.add(
                        NextAction(
                        text = split[1],
                        color = split[0],
                        url = split[2]
                    )
                    )
                }
                NextActions(list)
            } ?: NextActions(emptyList())
    }

    override suspend fun updateTimestamp() {
        val now = OffsetDateTime.now()
        val timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)
        context.dataStore.edit { settings ->
            settings[TIMESTAMP] = timestamp
        }
    }

    override suspend fun updateNextActions(nextActions: List<NextAction>) {
        context.dataStore.edit { settings ->
            val joined = nextActions.joinToString(separator = "***") {
                "${it.color}###${it.text}###${it.url}"
            }
            settings[NEXT_ACTIONS] = joined
        }
    }
}

private val TIMESTAMP = stringPreferencesKey("timestamp")
private val NEXT_ACTIONS = stringPreferencesKey("next_actions")
