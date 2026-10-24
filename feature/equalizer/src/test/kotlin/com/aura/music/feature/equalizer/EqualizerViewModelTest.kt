package com.aura.music.feature.equalizer

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class EqualizerViewModelTest {

    private lateinit var viewModel: EqualizerViewModel

    @Before
    fun setUp() {
        viewModel = EqualizerViewModel()
    }

    @Test
    fun initialState_hasDefaultValues() {
        val state = viewModel.uiState.value
        assertThat(state.isEnabled).isFalse()
        assertThat(state.bassBoost).isEqualTo(0)
        assertThat(state.virtualizer).isEqualTo(0)
        assertThat(state.selectedPreset).isEqualTo("Flat")
        assertThat(state.bands).isNotEmpty()
    }

    @Test
    fun toggleEnabled_updatesState() {
        viewModel.toggleEnabled(true)
        assertThat(viewModel.uiState.value.isEnabled).isTrue()

        viewModel.toggleEnabled(false)
        assertThat(viewModel.uiState.value.isEnabled).isFalse()
    }

    @Test
    fun onBandLevelChanged_updatesBandAndSwitchesToCustom() {
        val targetBand = 0
        val targetLevel: Short = 400

        viewModel.onBandLevelChanged(targetBand, targetLevel)

        val state = viewModel.uiState.value
        assertThat(state.selectedPreset).isEqualTo("Custom")
        assertThat(state.bands[targetBand].currentLevelMb).isEqualTo(targetLevel)
    }

    @Test
    fun onBassBoostChanged_clampsAndUpdatesState() {
        viewModel.onBassBoostChanged(500)
        assertThat(viewModel.uiState.value.bassBoost).isEqualTo(500)

        // Above max
        viewModel.onBassBoostChanged(1200)
        assertThat(viewModel.uiState.value.bassBoost).isEqualTo(1000)

        // Below min
        viewModel.onBassBoostChanged((-50).toShort())
        assertThat(viewModel.uiState.value.bassBoost).isEqualTo(0)
    }

    @Test
    fun onVirtualizerChanged_clampsAndUpdatesState() {
        viewModel.onVirtualizerChanged(750)
        assertThat(viewModel.uiState.value.virtualizer).isEqualTo(750)
    }

    @Test
    fun selectPreset_updatesSelectedPreset() {
        viewModel.selectPreset("Rock")
        assertThat(viewModel.uiState.value.selectedPreset).isEqualTo("Rock")
    }

    @Test
    fun reset_restoresFlatPresetAndZeroEnhancements() {
        viewModel.onBassBoostChanged(800)
        viewModel.onVirtualizerChanged(600)
        viewModel.selectPreset("Rock")

        viewModel.reset()

        val state = viewModel.uiState.value
        assertThat(state.bassBoost).isEqualTo(0)
        assertThat(state.virtualizer).isEqualTo(0)
        assertThat(state.selectedPreset).isEqualTo("Flat")
    }
}
