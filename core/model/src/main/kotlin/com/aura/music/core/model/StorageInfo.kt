package com.aura.music.core.model

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val isExternalSd: Boolean,
    val storagePath: String
) {
    val isLowStorage: Boolean
        get() = availableBytes < 500L * 1024L * 1024L // Less than 500MB is considered low storage threshold
}

data class AudioDiagnosticInfo(
    val playerState: String = "STATE_IDLE",
    val isPlaying: Boolean = false,
    val playWhenReady: Boolean = false,
    val playerVolume: Float = 1.0f,
    val audioSessionId: Int = 0,
    val deviceStreamVolume: String = "Unknown",
    val currentMediaUri: String = "",
    val contentResolverStatus: String = "Not Tested",
    val equalizerBypassed: Boolean = true,
    val lastError: String? = null
)

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val isCrossfadeEnabled: Boolean = false,
    val crossfadeDurationSeconds: Int = 3,
    val errorMessage: String? = null,
    val diagnosticInfo: AudioDiagnosticInfo = AudioDiagnosticInfo()
)
