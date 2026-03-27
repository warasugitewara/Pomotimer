package com.example.pomodoro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATION_ENABLED] ?: true }

    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SOUND_ENABLED] ?: true }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }
}
