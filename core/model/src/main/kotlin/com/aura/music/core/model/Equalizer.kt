package com.aura.music.core.model

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val minLevelMb: Short,
    val maxLevelMb: Short,
    val currentLevelMb: Short
) {
    fun withLevel(levelMb: Int): EqualizerBand {
        val clamped = levelMb.coerceIn(minLevelMb.toInt(), maxLevelMb.toInt()).toShort()
        return copy(currentLevelMb = clamped)
    }
}

data class EqualizerConfig(
    val enabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val presetName: String? = null
)
