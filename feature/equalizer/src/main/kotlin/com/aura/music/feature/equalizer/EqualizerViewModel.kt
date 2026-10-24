package com.aura.music.feature.equalizer

import androidx.lifecycle.ViewModel
import com.aura.music.core.playback.EqualizerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EqualizerViewModel(
    private val equalizerEngine: EqualizerEngine = EqualizerEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EqualizerUiState(
            isEnabled = false,
            bands = equalizerEngine.getBands(),
            bassBoost = 0,
            virtualizer = 0,
            selectedPreset = "Flat"
        )
    )
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    fun toggleEnabled(enabled: Boolean) {
        equalizerEngine.setEnabled(enabled)
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    fun onBandLevelChanged(bandIndex: Int, levelMb: Short) {
        equalizerEngine.setBandLevel(bandIndex, levelMb)
        _uiState.update { state ->
            val updatedBands = state.bands.map { band ->
                if (band.index == bandIndex) band.copy(currentLevelMb = levelMb) else band
            }
            state.copy(bands = updatedBands, selectedPreset = "Custom")
        }
    }

    fun onBassBoostChanged(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        equalizerEngine.setBassBoost(clamped)
        _uiState.update { it.copy(bassBoost = clamped) }
    }

    fun onVirtualizerChanged(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        equalizerEngine.setVirtualizer(clamped)
        _uiState.update { it.copy(virtualizer = clamped) }
    }

    fun selectPreset(preset: String) {
        equalizerEngine.applyPreset(preset)
        val refreshedBands = equalizerEngine.getBands()
        _uiState.update {
            it.copy(
                selectedPreset = preset,
                bands = refreshedBands
            )
        }
    }

    fun reset() {
        selectPreset("Flat")
        onBassBoostChanged(0)
        onVirtualizerChanged(0)
    }
}
