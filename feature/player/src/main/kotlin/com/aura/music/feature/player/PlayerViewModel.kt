package com.aura.music.feature.player

import androidx.lifecycle.ViewModel
import com.aura.music.core.model.PlaybackState
import com.aura.music.core.model.Track
import com.aura.music.core.playback.PlaybackManager
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(
    private val playbackManager: PlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    val currentTrack: StateFlow<Track?> = playbackManager.currentTrack
    val queue: StateFlow<List<Track>> = playbackManager.queue

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        playbackManager.setQueue(tracks, startIndex)
    }

    fun playTrack(track: Track) {
        playbackManager.playTrack(track)
    }

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    fun skipNext() {
        playbackManager.skipNext()
    }

    fun skipPrevious() {
        playbackManager.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun toggleFavorite(isFavorite: Boolean) {
        playbackManager.toggleFavorite(isFavorite)
    }
}
