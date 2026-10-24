package com.aura.music.core.playback

import android.media.AudioManager
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class AudioFocusControllerTest {

    private lateinit var focusController: AudioFocusController
    private var isPlaying = false
    private var currentVolume = 1.0f

    @Before
    fun setUp() {
        isPlaying = true
        currentVolume = 1.0f
        focusController = AudioFocusController(
            onPausePlayback = { isPlaying = false },
            onResumePlayback = { isPlaying = true },
            onSetVolume = { currentVolume = it }
        )
    }

    @Test
    fun `transient focus loss pauses playback and flags resume on gain`() {
        focusController.onPlaybackStarted()
        assertThat(isPlaying).isTrue()

        // Incoming phone call triggers transient loss
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        assertThat(isPlaying).isFalse()
        assertThat(focusController.resumeOnFocusGain).isTrue()

        // Call finishes, focus is regained
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertThat(isPlaying).isTrue()
        assertThat(currentVolume).isEqualTo(1.0f)
    }

    @Test
    fun `transient loss can duck lowers player volume without pausing`() {
        focusController.onPlaybackStarted()

        // Navigation notification or map prompt
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        assertThat(isPlaying).isTrue()
        assertThat(currentVolume).isEqualTo(0.2f)

        // Navigation prompt completes
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertThat(currentVolume).isEqualTo(1.0f)
    }

    @Test
    fun `permanent focus loss pauses playback and clears resume flag`() {
        focusController.onPlaybackStarted()

        // Another music app starts playing
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        assertThat(isPlaying).isFalse()
        assertThat(focusController.resumeOnFocusGain).isFalse()

        // Focus returned later should NOT auto-resume
        focusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertThat(isPlaying).isFalse()
    }
}
