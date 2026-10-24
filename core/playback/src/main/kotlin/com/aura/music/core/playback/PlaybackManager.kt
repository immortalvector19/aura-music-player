package com.aura.music.core.playback

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.aura.music.core.model.PlaybackState
import com.aura.music.core.model.RepeatMode
import com.aura.music.core.model.ShuffleMode
import com.aura.music.core.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Centralized audio playback manager that orchestrates playback state,
 * queue navigation, position polling, and foreground service lifecycle.
 */
class PlaybackManager(
    private val context: Context? = null,
    val player: AudioPlayer,
    val equalizerEngine: EqualizerEngine = EqualizerEngine(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AudioPlayerListener {

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var currentTrackIndex = -1
    private var positionUpdateJob: Job? = null

    init {
        player.setListener(this)
        instance = this
    }

    companion object {
        @Volatile
        var instance: PlaybackManager? = null
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        _queue.value = tracks
        if (tracks.isNotEmpty() && startIndex in tracks.indices) {
            playTrackAtIndex(startIndex)
        }
    }

    fun playTrack(track: Track) {
        val q = _queue.value
        val index = q.indexOfFirst { it.id == track.id }
        if (index != -1) {
            playTrackAtIndex(index)
        } else {
            val updatedQueue = q + track
            _queue.value = updatedQueue
            playTrackAtIndex(updatedQueue.lastIndex)
        }
    }

    private fun playTrackAtIndex(index: Int) {
        val q = _queue.value
        if (index !in q.indices) return
        currentTrackIndex = index
        val track = q[index]
        _currentTrack.value = track

        // Ensure Foreground Service is started for background playback
        context?.let { ctx ->
            try {
                val serviceIntent = Intent(ctx, AuraMediaService::class.java)
                ContextCompat.startForegroundService(ctx, serviceIntent)
            } catch (_: Exception) { }
        }

        player.setMediaItem(track)
        player.play()

        _playbackState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = track.durationMs,
                errorMessage = null
            )
        }
        startPositionPolling()
    }

    fun togglePlayPause() {
        if (player.isPlaying()) {
            player.pause()
            _playbackState.update { it.copy(isPlaying = false) }
            stopPositionPolling()
        } else {
            if (_currentTrack.value != null) {
                player.play()
                _playbackState.update { it.copy(isPlaying = true) }
                startPositionPolling()
            } else if (_queue.value.isNotEmpty()) {
                playTrackAtIndex(0)
            }
        }
    }

    fun pause() {
        player.pause()
        _playbackState.update { it.copy(isPlaying = false) }
        stopPositionPolling()
    }

    fun play() {
        player.play()
        _playbackState.update { it.copy(isPlaying = true) }
        startPositionPolling()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val state = _playbackState.value
        if (state.repeatMode == RepeatMode.ONE) {
            seekTo(0L)
            player.play()
            return
        }

        val nextIndex = if (state.shuffleMode == ShuffleMode.ON && q.size > 1) {
            var rand = (0 until q.size).random()
            while (rand == currentTrackIndex && q.size > 1) {
                rand = (0 until q.size).random()
            }
            rand
        } else {
            (currentTrackIndex + 1) % q.size
        }
        playTrackAtIndex(nextIndex)
    }

    fun skipPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (player.currentPosition() > 3000L) {
            seekTo(0L)
            _playbackState.update { it.copy(currentPositionMs = 0L) }
            return
        }

        val prevIndex = if (currentTrackIndex - 1 < 0) q.size - 1 else currentTrackIndex - 1
        playTrackAtIndex(prevIndex)
    }

    fun toggleRepeat() {
        _playbackState.update { state ->
            val next = when (state.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            state.copy(repeatMode = next)
        }
    }

    fun toggleShuffle() {
        _playbackState.update { state ->
            val next = when (state.shuffleMode) {
                ShuffleMode.OFF -> ShuffleMode.ON
                ShuffleMode.ON -> ShuffleMode.OFF
            }
            state.copy(shuffleMode = next)
        }
    }

    fun toggleFavorite(isFavorite: Boolean) {
        _currentTrack.update { it?.copy(isFavorite = isFavorite) }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean, isEnded: Boolean) {
        val duration = player.duration()
        _playbackState.update {
            it.copy(
                isPlaying = isPlaying,
                durationMs = if (duration > 0) duration else it.durationMs
            )
        }
        if (isPlaying) {
            startPositionPolling()
        } else {
            stopPositionPolling()
        }

        if (isEnded) {
            handleTrackEnded()
        }
    }

    private fun handleTrackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                player.play()
            }
            RepeatMode.ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                if (currentTrackIndex < _queue.value.lastIndex) {
                    skipNext()
                } else {
                    stopPositionPolling()
                    _playbackState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
                }
            }
        }
    }

    override fun onError(message: String) {
        _playbackState.update {
            it.copy(
                isPlaying = false,
                errorMessage = message
            )
        }
        stopPositionPolling()
    }

    fun clearError() {
        _playbackState.update { it.copy(errorMessage = null) }
    }

    private fun startPositionPolling() {
        positionUpdateJob?.cancel()
        positionUpdateJob = coroutineScope.launch {
            while (isActive && player.isPlaying()) {
                val pos = player.currentPosition()
                val dur = player.duration()
                _playbackState.update {
                    it.copy(
                        currentPositionMs = pos,
                        durationMs = if (dur > 0) dur else it.durationMs
                    )
                }
                delay(250L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun release() {
        stopPositionPolling()
        player.release()
    }
}
