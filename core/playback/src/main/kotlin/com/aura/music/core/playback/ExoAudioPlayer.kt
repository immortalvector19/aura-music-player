package com.aura.music.core.playback

import android.net.Uri
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
 */
@OptIn(UnstableApi::class)
class ExoAudioPlayer(
    val exoPlayer: ExoPlayer
) : AudioPlayer, Player.Listener {

    private var listener: AudioPlayerListener? = null

    init {
        exoPlayer.addListener(this)
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun setMediaItem(track: Track) {
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
    }

    override fun isPlaying(): Boolean = exoPlayer.isPlaying

    override fun currentPosition(): Long = exoPlayer.currentPosition.coerceAtLeast(0L)

    override fun duration(): Long = exoPlayer.duration.coerceAtLeast(0L)

    override fun release() {
        exoPlayer.removeListener(this)
        exoPlayer.release()
    }

    override fun setListener(listener: AudioPlayerListener) {
        this.listener = listener
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val isEnded = playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(exoPlayer.isPlaying, isEnded)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        val isEnded = exoPlayer.playbackState == Player.STATE_ENDED
        listener?.onPlaybackStateChanged(isPlaying, isEnded)
    }

    override fun onPlayerError(error: PlaybackException) {
        listener?.onError(error.localizedMessage ?: "Playback error")
    }
}
