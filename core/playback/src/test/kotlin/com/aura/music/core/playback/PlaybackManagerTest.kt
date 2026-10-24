package com.aura.music.core.playback

import com.aura.music.core.model.RepeatMode
import com.aura.music.core.model.ShuffleMode
import com.aura.music.core.model.Track
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeAudioPlayer : AudioPlayer {
    var isPlayingState = false
    var currentPos = 0L
    var currentDur = 60000L
    var lastMediaItemUri: String? = null
    private var playerListener: AudioPlayerListener? = null
    var isReleased = false

    override fun play() {
        isPlayingState = true
        playerListener?.onPlaybackStateChanged(isPlaying = true, isEnded = false)
    }

    override fun pause() {
        isPlayingState = false
        playerListener?.onPlaybackStateChanged(isPlaying = false, isEnded = false)
    }

    override fun seekTo(positionMs: Long) {
        currentPos = positionMs
    }

    override fun setMediaItem(track: Track) {
        lastMediaItemUri = track.mediaUri
        currentDur = track.durationMs
    }

    override fun isPlaying(): Boolean = isPlayingState
    override fun currentPosition(): Long = currentPos
    override fun duration(): Long = currentDur
    override fun release() { isReleased = true }
    override fun setListener(listener: AudioPlayerListener) {
        this.playerListener = listener
    }

    fun simulateEnd() {
        isPlayingState = false
        playerListener?.onPlaybackStateChanged(isPlaying = false, isEnded = true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackManagerTest {

    private lateinit var fakePlayer: FakeAudioPlayer
    private lateinit var playbackManager: PlaybackManager
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val track1 = Track(
        id = 1L,
        mediaUri = "android.resource://com.aura.music/raw/aura_acoustic_sunrise",
        title = "Acoustic Sunrise",
        artist = "Aura Sound",
        durationMs = 12000L
    )

    private val track2 = Track(
        id = 2L,
        mediaUri = "android.resource://com.aura.music/raw/aura_midnight_groove",
        title = "Midnight Groove",
        artist = "Aura Sound",
        durationMs = 15000L
    )

    @Before
    fun setUp() {
        fakePlayer = FakeAudioPlayer()
        playbackManager = PlaybackManager(
            context = null,
            player = fakePlayer,
            equalizerEngine = EqualizerEngine(),
            coroutineScope = testScope
        )
    }

    @org.junit.After
    fun tearDown() {
        playbackManager.release()
    }

    @Test
    fun `playTrack sets media item on player, calls play, and updates state`() = testScope.runTest {
        playbackManager.playTrack(track1)

        assertThat(fakePlayer.lastMediaItemUri).isEqualTo(track1.mediaUri)
        assertThat(fakePlayer.isPlaying()).isTrue()
        assertThat(playbackManager.currentTrack.value).isEqualTo(track1)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.queue.value).contains(track1)
        playbackManager.pause()
    }

    @Test
    fun `togglePlayPause toggles audio player between playing and paused`() = testScope.runTest {
        playbackManager.playTrack(track1)
        assertThat(fakePlayer.isPlaying()).isTrue()

        playbackManager.togglePlayPause()
        assertThat(fakePlayer.isPlaying()).isFalse()
        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()

        playbackManager.togglePlayPause()
        assertThat(fakePlayer.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        playbackManager.pause()
    }

    @Test
    fun `skipNext advances to next track in queue`() = testScope.runTest {
        playbackManager.setQueue(listOf(track1, track2), startIndex = 0)
        assertThat(playbackManager.currentTrack.value).isEqualTo(track1)

        playbackManager.skipNext()
        assertThat(playbackManager.currentTrack.value).isEqualTo(track2)
        assertThat(fakePlayer.lastMediaItemUri).isEqualTo(track2.mediaUri)
        playbackManager.pause()
    }

    @Test
    fun `skipPrevious restarts track if played more than 3 seconds`() = testScope.runTest {
        playbackManager.setQueue(listOf(track1, track2), startIndex = 1)
        fakePlayer.currentPos = 5000L

        playbackManager.skipPrevious()
        // Stays on track2 but seeks to 0
        assertThat(playbackManager.currentTrack.value).isEqualTo(track2)
        assertThat(playbackManager.playbackState.value.currentPositionMs).isEqualTo(0L)
        playbackManager.pause()
    }

    @Test
    fun `track end with RepeatMode ALL automatically advances queue`() = testScope.runTest {
        playbackManager.setQueue(listOf(track1, track2), startIndex = 0)
        playbackManager.toggleRepeat() // RepeatMode.ALL

        fakePlayer.simulateEnd()
        assertThat(playbackManager.currentTrack.value).isEqualTo(track2)
        playbackManager.pause()
    }

    @Test
    fun `toggleRepeat cycles through OFF, ALL, ONE`() = testScope.runTest {
        assertThat(playbackManager.playbackState.value.repeatMode).isEqualTo(RepeatMode.OFF)

        playbackManager.toggleRepeat()
        assertThat(playbackManager.playbackState.value.repeatMode).isEqualTo(RepeatMode.ALL)

        playbackManager.toggleRepeat()
        assertThat(playbackManager.playbackState.value.repeatMode).isEqualTo(RepeatMode.ONE)

        playbackManager.toggleRepeat()
        assertThat(playbackManager.playbackState.value.repeatMode).isEqualTo(RepeatMode.OFF)
    }

    @Test
    fun `toggleShuffle cycles between OFF and ON`() = testScope.runTest {
        assertThat(playbackManager.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.OFF)

        playbackManager.toggleShuffle()
        assertThat(playbackManager.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.ON)

        playbackManager.toggleShuffle()
        assertThat(playbackManager.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.OFF)
    }
}
