package com.aura.music.feature.settings

import com.aura.music.core.model.StorageInfo

data class SettingsUiState(
    val dataSaver: Boolean = false,
    val wifiOnlyDownloads: Boolean = true,
    val audioFocusDucking: Boolean = true,
    val bluetoothAutoResume: Boolean = true,
    val preferredLanguage: String = "system",
    val storageLocations: List<StorageInfo> = emptyList(),
    val selectedStoragePath: String = "",
    val hasLowStorage: Boolean = false,
    val cacheSizeMb: Long = 0L
)
