package com.aura.music.core.playback

import com.aura.music.core.model.AudioDiagnosticInfo
import com.aura.music.core.model.Track

/**
 * Clean architectural seam for audio playback abstraction.
 */
interface AudioPlayer {
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setMediaItem(track: Track)
    fun isPlaying(): Boolean
    fun currentPosition(): Long
    fun duration(): Long
    fun release()
    fun setListener(listener: AudioPlayerListener)
    fun getDiagnosticInfo(): AudioDiagnosticInfo = AudioDiagnosticInfo()
}

interface AudioPlayerListener {
    fun onPlaybackStateChanged(isPlaying: Boolean, isEnded: Boolean)
    fun onError(message: String)
    fun onDiagnosticInfoChanged(info: AudioDiagnosticInfo) {}
}
