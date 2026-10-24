package com.aura.music.core.playback

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aura.music.core.model.AudioDiagnosticInfo
import com.aura.music.core.model.Track

/**
 * Production implementation of AudioPlayer backed by Media3 ExoPlayer.
 * Provides explicit logging, state tracking, and error surfacing.
 */
@OptIn(UnstableApi::class)
class ExoAudioPlayer(
    val exoPlayer: ExoPlayer,
    val context: Context? = null,
    val equalizerEngine: EqualizerEngine? = null
) : AudioPlayer, Player.Listener {

    private var listener: AudioPlayerListener? = null
    private var lastTrack: Track? = null
    private var lastError: String? = null

    init {
        exoPlayer.addListener(this)
        Log.d(TAG, "ExoAudioPlayer initialized with ExoPlayer instance: ${exoPlayer.hashCode()}")
    }

    fun testContentUri(uriString: String): String {
        if (context == null) return "Context not provided"
        if (uriString.isBlank()) return "URI is blank"
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val available = stream.available()
                "SUCCESS (Readable, $available bytes)"
            } ?: "FAILED: openInputStream returned null"
        } catch (e: Exception) {
            "FAILED: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun getDeviceStreamVolume(): String {
        if (context == null) return "Unknown"
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return "AudioManager null"
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return "$current / $max"
    }

    override fun getDiagnosticInfo(): AudioDiagnosticInfo {
        return AudioDiagnosticInfo(
            playerState = stateToString(exoPlayer.playbackState),
            isPlaying = exoPlayer.isPlaying,
            playWhenReady = exoPlayer.playWhenReady,
            playerVolume = exoPlayer.volume,
            audioSessionId = exoPlayer.audioSessionId,
            deviceStreamVolume = getDeviceStreamVolume(),
            currentMediaUri = lastTrack?.mediaUri ?: "",
            contentResolverStatus = testContentUri(lastTrack?.mediaUri ?: ""),
            equalizerBypassed = !(equalizerEngine?.isEnabled ?: false),
            lastError = lastError
        )
    }

    override fun play() {
        if (exoPlayer.volume <= 0f) {
            Log.w(TAG, "ExoPlayer volume was 0f, resetting to 1.0f")
            exoPlayer.volume = 1.0f
        }

        val diag = getDiagnosticInfo()
        Log.i(DEBUG_TAG, """
            ==================== AURA AUDIO PIPELINE AUDIT ====================
            ACTION: play()
            Track: '${lastTrack?.title ?: "<Unknown>"}' (ID: ${lastTrack?.id ?: -1})
            URI: ${diag.currentMediaUri}
            ContentResolver Status: ${diag.contentResolverStatus}
            ExoPlayer State: ${diag.playerState}
            isPlaying: ${diag.isPlaying}
            playWhenReady: ${diag.playWhenReady}
            Player Volume: ${diag.playerVolume}
            Device STREAM_MUSIC Volume: ${diag.deviceStreamVolume}
            AudioSessionId: ${diag.audioSessionId}
            Equalizer Bypassed: ${diag.equalizerBypassed}
            Last Error: ${diag.lastError ?: "NONE"}
            ===================================================================
        """.trimIndent())

        exoPlayer.play()
        listener?.onDiagnosticInfoChanged(diag)
    }

    override fun pause() {
        Log.d(TAG, "pause() called on ExoPlayer")
        exoPlayer.pause()
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    override fun seekTo(positionMs: Long) {
        Log.d(TAG, "seekTo($positionMs ms)")
        exoPlayer.seekTo(positionMs)
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    override fun setMediaItem(track: Track) {
        lastTrack = track
        lastError = null

        val testStatus = testContentUri(track.mediaUri)
        Log.i(DEBUG_TAG, "setMediaItem() trackId=${track.id}, title='${track.title}', mediaUri='${track.mediaUri}', ContentResolver Test=$testStatus")

        if (track.mediaUri.isBlank()) {
            val err = "Cannot play '${track.title}': media URI is empty or missing"
            lastError = err
            Log.e(TAG, err)
            listener?.onError(err)
            listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
            return
        }

        try {
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(Uri.parse(track.mediaUri))
                .setMediaMetadata(metadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            Log.d(TAG, "MediaItem set and prepare() called for trackId=${track.id}")
        } catch (e: Exception) {
            val err = "Failed to load audio for '${track.title}': ${e.localizedMessage ?: "Invalid URI"}"
            lastError = err
            Log.e(TAG, err, e)
            listener?.onError(err)
        }
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    override fun isPlaying(): Boolean = exoPlayer.isPlaying

    override fun currentPosition(): Long = exoPlayer.currentPosition.coerceAtLeast(0L)

    override fun duration(): Long = exoPlayer.duration.coerceAtLeast(0L)

    override fun release() {
        Log.d(TAG, "Releasing ExoPlayer instance: ${exoPlayer.hashCode()}")
        exoPlayer.removeListener(this)
        exoPlayer.release()
    }

    override fun setListener(listener: AudioPlayerListener) {
        this.listener = listener
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateName = stateToString(playbackState)
        Log.i(DEBUG_TAG, "onPlaybackStateChanged: state=$stateName, isPlaying=${exoPlayer.isPlaying}, playWhenReady=${exoPlayer.playWhenReady}, audioSessionId=${exoPlayer.audioSessionId}")

        val isEnded = playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(exoPlayer.isPlaying, isEnded)
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.i(DEBUG_TAG, "onIsPlayingChanged: isPlaying=$isPlaying (state=${stateToString(exoPlayer.playbackState)}, volume=${exoPlayer.volume})")
        val isEnded = exoPlayer.playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(isPlaying, isEnded)
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    override fun onPlayerError(error: PlaybackException) {
        val errorDetails = "PlaybackException [errorCode=${error.errorCode}, errorName=${error.errorCodeName}]: ${error.localizedMessage ?: "Unknown audio error"}"
        lastError = errorDetails
        Log.e(DEBUG_TAG, "AUDIO ERROR DETECTED: $errorDetails", error)
        listener?.onError(errorDetails)
        listener?.onDiagnosticInfoChanged(getDiagnosticInfo())
    }

    companion object {
        private const val TAG = "AuraPlayback"
        const val DEBUG_TAG = "AuraAudioDebug"

        fun stateToString(state: Int): String = when (state) {
            Player.STATE_IDLE -> "STATE_IDLE"
            Player.STATE_BUFFERING -> "STATE_BUFFERING"
            Player.STATE_READY -> "STATE_READY"
            Player.STATE_ENDED -> "STATE_ENDED"
            else -> "STATE_UNKNOWN($state)"
        }
    }
}
