package com.aura.music.core.playback

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aura.music.core.model.Track

/**
 * Production implementation of AudioPlayer backed by Media3 ExoPlayer.
 * Provides explicit logging, state tracking, and error surfacing.
 */
@OptIn(UnstableApi::class)
class ExoAudioPlayer(
    val exoPlayer: ExoPlayer
) : AudioPlayer, Player.Listener {

    private var listener: AudioPlayerListener? = null

    init {
        exoPlayer.addListener(this)
        Log.d(TAG, "ExoAudioPlayer initialized with ExoPlayer instance: ${exoPlayer.hashCode()}")
    }

    override fun play() {
        Log.d(TAG, "play() called on ExoPlayer (current state=${stateToString(exoPlayer.playbackState)}, playWhenReady=${exoPlayer.playWhenReady}, volume=${exoPlayer.volume})")
        exoPlayer.play()
    }

    override fun pause() {
        Log.d(TAG, "pause() called on ExoPlayer")
        exoPlayer.pause()
    }

    override fun seekTo(positionMs: Long) {
        Log.d(TAG, "seekTo($positionMs ms)")
        exoPlayer.seekTo(positionMs)
    }

    override fun setMediaItem(track: Track) {
        Log.d(TAG, "setMediaItem() trackId=${track.id}, title='${track.title}', mediaUri='${track.mediaUri}'")

        if (track.mediaUri.isBlank()) {
            val err = "Cannot play '${track.title}': media URI is empty or missing"
            Log.e(TAG, err)
            listener?.onError(err)
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
            Log.e(TAG, err, e)
            listener?.onError(err)
        }
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
        Log.d(TAG, "onPlaybackStateChanged: state=$stateName, isPlaying=${exoPlayer.isPlaying}, playWhenReady=${exoPlayer.playWhenReady}")

        val isEnded = playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(exoPlayer.isPlaying, isEnded)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "onIsPlayingChanged: isPlaying=$isPlaying (state=${stateToString(exoPlayer.playbackState)})")
        val isEnded = exoPlayer.playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(isPlaying, isEnded)
    }

    override fun onPlayerError(error: PlaybackException) {
        val errorDetails = "PlaybackException [errorCode=${error.errorCode}, errorName=${error.errorCodeName}]: ${error.localizedMessage ?: "Unknown audio error"}"
        Log.e(TAG, errorDetails, error)
        listener?.onError(errorDetails)
    }

    companion object {
        private const val TAG = "AuraPlayback"

        fun stateToString(state: Int): String = when (state) {
            Player.STATE_IDLE -> "STATE_IDLE"
            Player.STATE_BUFFERING -> "STATE_BUFFERING"
            Player.STATE_READY -> "STATE_READY"
            Player.STATE_ENDED -> "STATE_ENDED"
            else -> "STATE_UNKNOWN($state)"
        }
    }
}
