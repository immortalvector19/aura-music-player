package com.aura.music.core.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.aura.music.core.model.EqualizerBand
import com.aura.music.core.model.EqualizerConfig

class EqualizerEngine {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    var currentSessionId: Int = 0
        private set

    var isEnabled: Boolean = false
        private set

    val isAttached: Boolean
        get() = equalizer != null || bassBoost != null || virtualizer != null

    fun bindToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (audioSessionId == currentSessionId && isAttached) return

        currentSessionId = audioSessionId

        // Fully bypass audio effects if equalizer is not enabled by user
        if (!isEnabled) {
            releaseEffects()
            return
        }

        attachEffects(audioSessionId)
    }

    private fun attachEffects(audioSessionId: Int) {
        releaseEffects()
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = isEnabled
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
        } catch (e: Exception) {
            // AudioEffects may fail on custom hardware/ROMs; gracefully fall back
            releaseEffects()
        }
    }

    private fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
        }
    }

    fun getBands(): List<EqualizerBand> {
        val eq = equalizer ?: return createDefaultFallbackBands()
        val numBands = eq.numberOfBands.toInt()
        val bandLevelRange = eq.bandLevelRange // Array of 2 shorts [min, max] in millibels
        val minLevel = bandLevelRange.getOrNull(0) ?: -1500
        val maxLevel = bandLevelRange.getOrNull(1) ?: 1500

        val bands = mutableListOf<EqualizerBand>()
        for (i in 0 until numBands) {
            val centerFreqMhz = eq.getCenterFreq(i.toShort()) // in milliHertz
            val centerFreqHz = centerFreqMhz / 1000
            val currentLevel = eq.getBandLevel(i.toShort())
            bands.add(
                EqualizerBand(
                    index = i,
                    centerFreqHz = centerFreqHz,
                    minLevelMb = minLevel,
                    maxLevelMb = maxLevel,
                    currentLevelMb = currentLevel
                )
            )
        }
        return bands
    }

    fun setBandLevel(bandIndex: Int, levelMb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), levelMb)
        } catch (e: Exception) {
            // Ignore hardware audio effect failure
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000))
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setVirtualizer(strength: Short) {
        try {
            virtualizer?.setStrength(strength.coerceIn(0, 1000))
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun applyPreset(presetName: String) {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands.toInt()
        val levels = when (presetName.lowercase()) {
            "bass boost" -> listOf(600, 400, 100, 0, -200)
            "rock" -> listOf(400, 200, -100, 200, 500)
            "pop" -> listOf(-100, 200, 500, 300, -100)
            "jazz" -> listOf(300, 100, -200, 200, 400)
            "vocal boost" -> listOf(-200, 0, 600, 400, 100)
            else -> List(numBands) { 0 } // Flat
        }

        for (i in 0 until numBands) {
            val level = levels.getOrNull(i)?.toShort() ?: 0.toShort()
            setBandLevel(i, level)
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        if (enabled) {
            if (currentSessionId != 0 && !isAttached) {
                attachEffects(currentSessionId)
            } else {
                try {
                    equalizer?.enabled = true
                    bassBoost?.enabled = true
                    virtualizer?.enabled = true
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } else {
            // Complete bypass: release effects from audio hardware session so audio track is 100% untouched
            releaseEffects()
        }
    }

    fun release() {
        releaseEffects()
        currentSessionId = 0
    }

    private fun createDefaultFallbackBands(): List<EqualizerBand> {
        val defaultFreqs = listOf(60, 230, 910, 3600, 14000)
        return defaultFreqs.mapIndexed { index, freq ->
            EqualizerBand(
                index = index,
                centerFreqHz = freq,
                minLevelMb = -1500,
                maxLevelMb = 1500,
                currentLevelMb = 0
            )
        }
    }
}
