package com.aura.music.core.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class AudioFocusController(
    private val context: Context? = null,
    private val onPausePlayback: () -> Unit,
    private val onResumePlayback: () -> Unit,
    private val onSetVolume: (Float) -> Unit
) : AudioManager.OnAudioFocusChangeListener {

    var resumeOnFocusGain: Boolean = false
        private set

    private var hasAudioFocus: Boolean = false
    private var audioFocusRequest: AudioFocusRequest? = null

    fun onPlaybackStarted() {
        resumeOnFocusGain = false
        hasAudioFocus = true
    }

    fun onPlaybackStopped() {
        hasAudioFocus = false
        resumeOnFocusGain = false
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                onSetVolume(1.0f)
                if (resumeOnFocusGain) {
                    resumeOnFocusGain = false
                    onResumePlayback()
                }
                hasAudioFocus = true
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                resumeOnFocusGain = hasAudioFocus
                hasAudioFocus = false
                onPausePlayback()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower volume to 20% for transient navigation / assistant prompts
                onSetVolume(0.2f)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                hasAudioFocus = false
                onPausePlayback()
            }
        }
    }

    fun requestFocus(): Boolean {
        if (context == null) return true
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()

            audioFocusRequest = request
            val res = audioManager.requestAudioFocus(request)
            res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val res = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun abandonFocus() {
        if (context == null) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
        hasAudioFocus = false
        resumeOnFocusGain = false
    }
}
