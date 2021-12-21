package com.dbottillo.notionalert.feature.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dbottillo.notionalert.NextAction
import com.dbottillo.notionalert.NextActions
import com.dbottillo.notionalert.NextActionsSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Home")

private const val DATA_STORE_FILE_NAME = "next_actions.pb"

val Context.nextActionsDataStore: DataStore<NextActions> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = NextActionsSerializer
)

class HomeStorageImpl constructor(
    private val context: Context
) : HomeStorage {

    override val timestamp: Flow<OffsetDateTime> = context.dataStore.data
        .map { preferences ->
            val value = preferences[TIMESTAMP] ?: ""
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }

    override val nextActionsFlow: Flow<NextActions> = context.nextActionsDataStore.data

    override suspend fun updateTimestamp() {
        val now = OffsetDateTime.now()
        val timestamp = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)
        context.dataStore.edit { settings ->
            settings[TIMESTAMP] = timestamp
        }
    }

    override suspend fun updateNextActions(nextActions: List<NextAction>) {
        context.nextActionsDataStore.updateData { data ->
            data.toBuilder().clearActions().addAllActions(nextActions).build()
        }
    }
}

private val TIMESTAMP = stringPreferencesKey("timestamp")
