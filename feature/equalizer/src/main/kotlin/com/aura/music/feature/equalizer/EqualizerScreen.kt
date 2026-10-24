package com.aura.music.feature.equalizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.music.core.model.EqualizerBand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerRoute(
    viewModel: EqualizerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EqualizerScreen(
        uiState = uiState,
        onToggleEnabled = viewModel::toggleEnabled,
        onBandLevelChanged = viewModel::onBandLevelChanged,
        onBassBoostChanged = viewModel::onBassBoostChanged,
        onVirtualizerChanged = viewModel::onVirtualizerChanged,
        onSelectPreset = viewModel::selectPreset,
        onReset = viewModel::reset,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    uiState: EqualizerUiState,
    onToggleEnabled: (Boolean) -> Unit,
    onBandLevelChanged: (bandIndex: Int, levelMb: Short) -> Unit,
    onBassBoostChanged: (Short) -> Unit,
    onVirtualizerChanged: (Short) -> Unit,
    onSelectPreset: (String) -> Unit,
    onReset: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", style = MaterialTheme.typography.titleLarge) },
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
                },
                actions = {
                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .semantics { contentDescription = "Toggle Equalizer" }
                    )
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Presets Horizontal Row
            Text(
                text = "Presets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availablePresets.forEach { preset ->
                    FilterChip(
                        selected = uiState.selectedPreset == preset,
                        onClick = {
                            if (uiState.isEnabled) {
                                onSelectPreset(preset)
                            }
                        },
                        label = { Text(preset) },
                        enabled = uiState.isEnabled,
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bands Control
            Text(
                text = "Frequencies",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.bands.isEmpty()) {
                        Text(
                            text = "No audio effects bands available on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.bands.forEach { band ->
                            BandSliderRow(
                                band = band,
                                isEnabled = uiState.isEnabled,
                                onLevelChanged = { newLevel ->
                                    onBandLevelChanged(band.index, newLevel)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Enhancement Effects (Bass Boost & Virtualizer)
            Text(
                text = "Enhancements",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Bass Boost
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bass Boost", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${(uiState.bassBoost / 10)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = uiState.bassBoost.toFloat(),
                                onValueChange = { onBassBoostChanged(it.toInt().toShort()) },
                                valueRange = 0f..1000f,
                                enabled = uiState.isEnabled,
                                modifier = Modifier.semantics {
                                    contentDescription = "Bass Boost ${(uiState.bassBoost / 10)}%"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Virtualizer (3D Surround)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Virtualizer", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${(uiState.virtualizer / 10)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Slider(
                                value = uiState.virtualizer.toFloat(),
                                onValueChange = { onVirtualizerChanged(it.toInt().toShort()) },
                                valueRange = 0f..1000f,
                                enabled = uiState.isEnabled,
                                modifier = Modifier.semantics {
                                    contentDescription = "Virtualizer ${(uiState.virtualizer / 10)}%"
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Button
            OutlinedButton(
                onClick = onReset,
                enabled = uiState.isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Flat")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BandSliderRow(
    band: EqualizerBand,
    isEnabled: Boolean,
    onLevelChanged: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    val freqFormatted = formatFrequency(band.centerFreqHz)
    val dbValue = band.currentLevelMb / 100 // 100 millibels = 1 dB
    val dbFormatted = if (dbValue > 0) "+$dbValue dB" else "$dbValue dB"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = freqFormatted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(64.dp)
        )

        Slider(
            value = band.currentLevelMb.toFloat(),
            onValueChange = { onLevelChanged(it.toInt().toShort()) },
            valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
            enabled = isEnabled,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "$freqFormatted at $dbFormatted"
                }
        )

        Text(
            text = dbFormatted,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .width(52.dp)
                .padding(start = 8.dp)
        )
    }
}

private fun formatFrequency(freqHz: Int): String {
    return if (freqHz >= 1000) {
        val kHz = freqHz / 1000f
        if (kHz == kHz.toInt().toFloat()) "${kHz.toInt()} kHz" else "%.1f kHz".format(kHz)
    } else {
        "$freqHz Hz"
    }
}
