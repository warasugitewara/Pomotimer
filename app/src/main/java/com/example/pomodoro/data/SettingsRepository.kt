package com.example.pomodoro.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val NOTIFICATION_ENABLED  = booleanPreferencesKey("notification_enabled")
        val SOUND_ENABLED         = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED     = booleanPreferencesKey("vibration_enabled")
        val APP_THEME             = stringPreferencesKey("app_theme")
        val CUSTOM_BG_COLOR       = stringPreferencesKey("custom_bg_color")
        val CUSTOM_TEXT_COLOR     = stringPreferencesKey("custom_text_color")
        val CUSTOM_ACCENT_COLOR   = stringPreferencesKey("custom_accent_color")
        val DISCORD_RPC_ENABLED   = booleanPreferencesKey("discord_rpc_enabled")
        val DISCORD_BRIDGE_HOST   = stringPreferencesKey("discord_bridge_host")
        val DISCORD_BRIDGE_PORT   = stringPreferencesKey("discord_bridge_port")
        val DISCORD_BRIDGE_HTTPS  = booleanPreferencesKey("discord_bridge_https")
        val DISCORD_BRIDGE_TOKEN  = stringPreferencesKey("discord_bridge_token")
        val AUTO_START_BREAK      = booleanPreferencesKey("auto_start_break")
        val AUTO_START_WORK       = booleanPreferencesKey("auto_start_work")
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATION_ENABLED] ?: true }
    val soundEnabled:        Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED]         ?: true }
    val vibrationEnabled:    Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_ENABLED]     ?: true }
    val appTheme:            Flow<String>  = context.dataStore.data.map { it[APP_THEME]             ?: "LIGHT" }
    val customBgColor:       Flow<String>  = context.dataStore.data.map { it[CUSTOM_BG_COLOR]       ?: "#FAFAFA" }
    val customTextColor:     Flow<String>  = context.dataStore.data.map { it[CUSTOM_TEXT_COLOR]     ?: "#212121" }
    val customAccentColor:   Flow<String>  = context.dataStore.data.map { it[CUSTOM_ACCENT_COLOR]   ?: "#E53935" }
    val discordRpcEnabled:   Flow<Boolean> = context.dataStore.data.map { it[DISCORD_RPC_ENABLED]   ?: false }
    val discordBridgeHost:   Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_HOST]   ?: "" }
    val discordBridgePort:   Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_PORT]   ?: "" }
    val discordBridgeHttps:  Flow<Boolean> = context.dataStore.data.map { it[DISCORD_BRIDGE_HTTPS]  ?: false }
    val discordBridgeToken:  Flow<String>  = context.dataStore.data.map { it[DISCORD_BRIDGE_TOKEN]  ?: "" }
    val autoStartBreak:      Flow<Boolean> = context.dataStore.data.map { it[AUTO_START_BREAK]      ?: false }
    val autoStartWork:       Flow<Boolean> = context.dataStore.data.map { it[AUTO_START_WORK]       ?: false }

    suspend fun setNotificationEnabled(v: Boolean) = context.dataStore.edit { it[NOTIFICATION_ENABLED] = v }
    suspend fun setSoundEnabled(v: Boolean)        = context.dataStore.edit { it[SOUND_ENABLED]         = v }
    suspend fun setVibrationEnabled(v: Boolean)    = context.dataStore.edit { it[VIBRATION_ENABLED]     = v }
    suspend fun setAppTheme(v: String)             = context.dataStore.edit { it[APP_THEME]             = v }
    suspend fun setCustomBgColor(v: String)        = context.dataStore.edit { it[CUSTOM_BG_COLOR]       = v }
    suspend fun setCustomTextColor(v: String)      = context.dataStore.edit { it[CUSTOM_TEXT_COLOR]     = v }
    suspend fun setCustomAccentColor(v: String)    = context.dataStore.edit { it[CUSTOM_ACCENT_COLOR]   = v }
    suspend fun setDiscordRpcEnabled(v: Boolean)   = context.dataStore.edit { it[DISCORD_RPC_ENABLED]   = v }
    suspend fun setDiscordBridgeHost(v: String)    = context.dataStore.edit { it[DISCORD_BRIDGE_HOST]   = v }
    suspend fun setDiscordBridgePort(v: String)    = context.dataStore.edit { it[DISCORD_BRIDGE_PORT]   = v }
    suspend fun setDiscordBridgeHttps(v: Boolean)  = context.dataStore.edit { it[DISCORD_BRIDGE_HTTPS]  = v }
    suspend fun setDiscordBridgeToken(v: String)   = context.dataStore.edit { it[DISCORD_BRIDGE_TOKEN]  = v }
    suspend fun setAutoStartBreak(v: Boolean)      = context.dataStore.edit { it[AUTO_START_BREAK]      = v }
    suspend fun setAutoStartWork(v: Boolean)       = context.dataStore.edit { it[AUTO_START_WORK]       = v }
}
