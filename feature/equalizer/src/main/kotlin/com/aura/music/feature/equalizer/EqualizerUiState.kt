package com.aura.music.feature.equalizer

import com.aura.music.core.model.EqualizerBand
import com.aura.music.core.model.EqualizerConfig

data class EqualizerUiState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoost: Short = 0,
    val virtualizer: Short = 0,
    val selectedPreset: String = "Flat",
    val availablePresets: List<String> = listOf(
        "Flat",
        "Bass Boost",
        "Rock",
        "Pop",
        "Jazz",
        "Vocal Boost"
    )
) {
    fun toConfig(): EqualizerConfig = EqualizerConfig(
        enabled = isEnabled,
        bands = bands,
        bassBoostStrength = bassBoost,
        virtualizerStrength = virtualizer,
        presetName = selectedPreset
    )
}
