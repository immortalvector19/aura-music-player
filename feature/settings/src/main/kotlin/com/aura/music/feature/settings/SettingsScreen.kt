package com.aura.music.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.music.core.model.AudioDiagnosticInfo
import com.aura.music.core.model.StorageInfo
import com.aura.music.core.ui.AudioDiagnosticsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    diagnosticInfo: AudioDiagnosticInfo = AudioDiagnosticInfo()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        diagnosticInfo = diagnosticInfo,
        onToggleDataSaver = viewModel::toggleDataSaver,
        onToggleWifiOnly = viewModel::toggleWifiOnlyDownloads,
        onToggleAudioFocusDucking = viewModel::toggleAudioFocusDucking,
        onToggleBluetoothResume = viewModel::toggleBluetoothAutoResume,
        onSelectLanguage = viewModel::setPreferredLanguage,
        onSelectStorage = viewModel::selectStorageLocation,
        onClearCache = viewModel::clearCache,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    diagnosticInfo: AudioDiagnosticInfo = AudioDiagnosticInfo(),
    onToggleDataSaver: (Boolean) -> Unit,
    onToggleWifiOnly: (Boolean) -> Unit,
    onToggleAudioFocusDucking: (Boolean) -> Unit,
    onToggleBluetoothResume: (Boolean) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectStorage: (String) -> Unit,
    onClearCache: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Low Storage Banner
            if (uiState.hasLowStorage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Low storage space (<500MB). Streaming cache and downloads may be limited.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Section 1: Playback & Audio Focus
            SettingsSectionHeader(title = "Playback & Audio")

            SettingsSwitchItem(
                icon = Icons.AutoMirrored.Filled.VolumeDown,
                title = "Audio Focus Ducking",
                subtitle = "Lower volume rather than pausing during phone notifications and navigation prompts",
                checked = uiState.audioFocusDucking,
                onCheckedChange = onToggleAudioFocusDucking
            )

            SettingsSwitchItem(
                icon = Icons.Default.BluetoothAudio,
                title = "Bluetooth Auto-Resume",
                subtitle = "Automatically resume playback when reconnecting to paired Bluetooth headsets or car audio",
                checked = uiState.bluetoothAutoResume,
                onCheckedChange = onToggleBluetoothResume
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Data & Network
            SettingsSectionHeader(title = "Data Saver & Downloads")

            SettingsSwitchItem(
                icon = Icons.Default.DataSaverOn,
                title = "Data Saver Mode",
                subtitle = "Stream lower bitrates (96 kbps) on mobile cellular connections to preserve data",
                checked = uiState.dataSaver,
                onCheckedChange = onToggleDataSaver
            )

            SettingsSwitchItem(
                icon = Icons.Default.Wifi,
                title = "Download over Wi-Fi Only",
                subtitle = "Prevent offline track downloads from consuming cellular mobile data plans",
                checked = uiState.wifiOnlyDownloads,
                onCheckedChange = onToggleWifiOnly
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Storage & Locations
            SettingsSectionHeader(title = "Storage & Offline Locations")

            if (uiState.storageLocations.isNotEmpty()) {
                uiState.storageLocations.forEach { storage ->
                    StorageOptionItem(
                        storage = storage,
                        isSelected = uiState.selectedStoragePath == storage.storagePath ||
                            (uiState.selectedStoragePath.isEmpty() && !storage.isExternalSd),
                        onSelect = { onSelectStorage(storage.storagePath) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Cache clearing action
            OutlinedButton(
                onClick = onClearCache,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Media3 Streaming Cache")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: System & Language
            SettingsSectionHeader(title = "Language & System")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { showLanguageDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("App Language", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        formatLanguageName(uiState.preferredLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 5: Developer & Diagnostics
            SettingsSectionHeader(title = "Developer / Diagnostics")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { showDiagnosticsDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Audio Pipeline Audit", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Live inspection of player state, hardware AudioSession, stream volume, and ContentResolver probe",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App About Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aura Music Player",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Version 1.0.0 (Native Android • Media3 • Compose)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Language Picker Dialog
        if (showLanguageDialog) {
            LanguagePickerDialog(
                currentLanguage = uiState.preferredLanguage,
                onSelectLanguage = {
                    onSelectLanguage(it)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        // Audio Diagnostics Dialog
        if (showDiagnosticsDialog) {
            AudioDiagnosticsDialog(
                diagnosticInfo = diagnosticInfo,
                onDismiss = { showDiagnosticsDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = title }
        )
    }
}

@Composable
private fun StorageOptionItem(
    storage: StorageInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onSelect)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SdCard,
                contentDescription = null,
                tint = if (storage.isExternalSd) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (storage.isExternalSd) "MicroSD Card" else "Internal Storage",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${formatBytes(storage.availableBytes)} free of ${formatBytes(storage.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "system" to "System Default",
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "ja" to "日本語",
        "zh" to "中文",
        "hi" to "हिन्दी"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Language") },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(code) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = { onSelectLanguage(code) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatLanguageName(code: String): String = when (code) {
    "en" -> "English"
    "es" -> "Español"
    "fr" -> "Français"
    "de" -> "Deutsch"
    "ja" -> "日本語"
    "zh" -> "中文"
    "hi" -> "हिन्दी"
    else -> "System Default"
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        "%.1f GB".format(gb)
    } else {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}
