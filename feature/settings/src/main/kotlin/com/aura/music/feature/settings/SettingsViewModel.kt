package com.aura.music.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.music.core.data.repository.PreferencesRepository
import com.aura.music.core.data.storage.StorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val storageManager: StorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        loadStorageInfo()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        dataSaver = prefs.dataSaverEnabled,
                        wifiOnlyDownloads = prefs.wifiOnlyDownloads,
                        audioFocusDucking = prefs.audioFocusDucking,
                        bluetoothAutoResume = prefs.bluetoothAutoResume,
                        preferredLanguage = prefs.preferredLanguage,
                        selectedStoragePath = prefs.selectedStoragePath
                    )
                }
            }
        }
    }

    fun loadStorageInfo() {
        viewModelScope.launch {
            val locations = storageManager.getAvailableStorageLocations()
            val hasLow = locations.any { it.isLowStorage }
            _uiState.update { current ->
                current.copy(
                    storageLocations = locations,
                    hasLowStorage = hasLow
                )
            }
        }
    }

    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDataSaverEnabled(enabled)
        }
    }

    fun toggleWifiOnlyDownloads(wifiOnly: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWifiOnlyDownloads(wifiOnly)
        }
    }

    fun toggleAudioFocusDucking(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAudioFocusDucking(enabled)
        }
    }

    fun toggleBluetoothAutoResume(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBluetoothAutoResume(enabled)
        }
    }

    fun setPreferredLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.setPreferredLanguage(languageCode)
        }
    }

    fun selectStorageLocation(storagePath: String) {
        viewModelScope.launch {
            preferencesRepository.setSelectedStoragePath(storagePath)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(cacheSizeMb = 0L) }
        }
    }
}
