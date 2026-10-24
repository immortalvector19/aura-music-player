package com.aura.music.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_preferences")

data class UserPreferences(
    val dataSaverEnabled: Boolean = false,
    val wifiOnlyDownloads: Boolean = true,
    val preferredLanguage: String = "system",
    val audioFocusDucking: Boolean = true,
    val bluetoothAutoResume: Boolean = true,
    val selectedStoragePath: String = ""
)

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DATA_SAVER_ENABLED = booleanPreferencesKey("data_saver_enabled")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val AUDIO_FOCUS_DUCKING = booleanPreferencesKey("audio_focus_ducking")
        val BLUETOOTH_AUTO_RESUME = booleanPreferencesKey("bluetooth_auto_resume")
        val SELECTED_STORAGE_PATH = stringPreferencesKey("selected_storage_path")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                dataSaverEnabled = preferences[PreferencesKeys.DATA_SAVER_ENABLED] ?: false,
                wifiOnlyDownloads = preferences[PreferencesKeys.WIFI_ONLY_DOWNLOADS] ?: true,
                preferredLanguage = preferences[PreferencesKeys.PREFERRED_LANGUAGE] ?: "system",
                audioFocusDucking = preferences[PreferencesKeys.AUDIO_FOCUS_DUCKING] ?: true,
                bluetoothAutoResume = preferences[PreferencesKeys.BLUETOOTH_AUTO_RESUME] ?: true,
                selectedStoragePath = preferences[PreferencesKeys.SELECTED_STORAGE_PATH] ?: ""
            )
        }

    suspend fun setDataSaverEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_SAVER_ENABLED] = enabled
        }
    }

    suspend fun setWifiOnlyDownloads(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_ONLY_DOWNLOADS] = wifiOnly
        }
    }

    suspend fun setPreferredLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_LANGUAGE] = languageCode
        }
    }

    suspend fun setAudioFocusDucking(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_FOCUS_DUCKING] = enabled
        }
    }

    suspend fun setBluetoothAutoResume(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUETOOTH_AUTO_RESUME] = enabled
        }
    }

    suspend fun setSelectedStoragePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_STORAGE_PATH] = path
        }
    }
}
