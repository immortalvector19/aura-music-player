package com.aura.music.feature.player

import com.aura.music.core.model.RepeatMode
import com.aura.music.core.model.ShuffleMode
import com.aura.music.core.model.Track
import com.aura.music.core.playback.AudioPlayer
import com.aura.music.core.playback.AudioPlayerListener
import com.aura.music.core.playback.EqualizerEngine
import com.aura.music.core.playback.PlaybackManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class TestAudioPlayer : AudioPlayer {
    var isPlayingState = false
    var currentPos = 0L
    var currentDur = 0L
    private var listener: AudioPlayerListener? = null

    override fun play() {
        isPlayingState = true
        listener?.onPlaybackStateChanged(isPlaying = true, isEnded = false)
    }

    override fun pause() {
        isPlayingState = false
        listener?.onPlaybackStateChanged(isPlaying = false, isEnded = false)
    }

    override fun seekTo(positionMs: Long) {
        currentPos = positionMs
    }

    override fun setMediaItem(track: Track) {
        currentDur = track.durationMs
    }

    override fun isPlaying(): Boolean = isPlayingState
    override fun currentPosition(): Long = currentPos
    override fun duration(): Long = currentDur
    override fun release() {}
    override fun setListener(listener: AudioPlayerListener) {
        this.listener = listener
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private lateinit var fakePlayer: TestAudioPlayer
    private lateinit var playbackManager: PlaybackManager
    private lateinit var viewModel: PlayerViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val track1 = Track(
        id = 1L,
        title = "Solaris",
        artist = "Photay",
        album = "Onism",
        durationMs = 240_000L,
        mediaUri = "content://media/external/audio/media/1",
        albumArtUri = null,
        year = 2017
    )

    private val track2 = Track(
        id = 2L,
        title = "Aura",
        artist = "Bicep",
        album = "Bicep",
        durationMs = 300_000L,
        mediaUri = "content://media/external/audio/media/2",
        albumArtUri = null,
        year = 2017
    )

    @Before
    fun setUp() {
        fakePlayer = TestAudioPlayer()
        playbackManager = PlaybackManager(
            context = null,
            player = fakePlayer,
            equalizerEngine = EqualizerEngine(),
            coroutineScope = testScope
        )
        viewModel = PlayerViewModel(playbackManager)
    }

    @After
    fun tearDown() {
        playbackManager.release()
    }

    @Test
    fun setQueue_setsQueueAndPlaysFirstTrack() = testScope.runTest {
        viewModel.setQueue(listOf(track1, track2), startIndex = 0)

        assertThat(viewModel.queue.value).hasSize(2)
        assertThat(viewModel.currentTrack.value).isEqualTo(track1)
        assertThat(viewModel.playbackState.value.isPlaying).isTrue()
        assertThat(viewModel.playbackState.value.durationMs).isEqualTo(240_000L)
        viewModel.togglePlayPause()
    }

    @Test
    fun playTrack_updatesCurrentTrackAndState() = testScope.runTest {
        viewModel.playTrack(track1)

        assertThat(viewModel.currentTrack.value).isEqualTo(track1)
        assertThat(viewModel.playbackState.value.isPlaying).isTrue()
        viewModel.togglePlayPause()
    }

    @Test
    fun togglePlayPause_invertsPlayingState() = testScope.runTest {
        viewModel.playTrack(track1)
        assertThat(viewModel.playbackState.value.isPlaying).isTrue()

        viewModel.togglePlayPause()
        assertThat(viewModel.playbackState.value.isPlaying).isFalse()

        viewModel.togglePlayPause()
        assertThat(viewModel.playbackState.value.isPlaying).isTrue()
        viewModel.togglePlayPause()
    }

    @Test
    fun skipNext_advancesQueueAndCycles() = testScope.runTest {
        viewModel.setQueue(listOf(track1, track2), startIndex = 0)
        assertThat(viewModel.currentTrack.value).isEqualTo(track1)

        viewModel.skipNext()
        assertThat(viewModel.currentTrack.value).isEqualTo(track2)

        viewModel.skipNext() // Cycles back to start
        assertThat(viewModel.currentTrack.value).isEqualTo(track1)
        viewModel.togglePlayPause()
    }

    @Test
    fun skipPrevious_goesBackOrCyclesToEnd() = testScope.runTest {
        viewModel.setQueue(listOf(track1, track2), startIndex = 1)
        assertThat(viewModel.currentTrack.value).isEqualTo(track2)

        viewModel.skipPrevious()
        assertThat(viewModel.currentTrack.value).isEqualTo(track1)

        viewModel.skipPrevious() // Cycles to end
        assertThat(viewModel.currentTrack.value).isEqualTo(track2)
        viewModel.togglePlayPause()
    }

    @Test
    fun toggleRepeat_cyclesModes() = testScope.runTest {
        assertThat(viewModel.playbackState.value.repeatMode).isEqualTo(RepeatMode.OFF)

        viewModel.toggleRepeat()
        assertThat(viewModel.playbackState.value.repeatMode).isEqualTo(RepeatMode.ALL)

        viewModel.toggleRepeat()
        assertThat(viewModel.playbackState.value.repeatMode).isEqualTo(RepeatMode.ONE)

        viewModel.toggleRepeat()
        assertThat(viewModel.playbackState.value.repeatMode).isEqualTo(RepeatMode.OFF)
    }

    @Test
    fun toggleShuffle_cyclesModes() = testScope.runTest {
        assertThat(viewModel.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.OFF)

        viewModel.toggleShuffle()
        assertThat(viewModel.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.ON)

        viewModel.toggleShuffle()
        assertThat(viewModel.playbackState.value.shuffleMode).isEqualTo(ShuffleMode.OFF)
    }

    @Test
    fun seekTo_updatesPosition() = testScope.runTest {
        viewModel.playTrack(track1)
        viewModel.seekTo(120_000L)

        assertThat(viewModel.playbackState.value.currentPositionMs).isEqualTo(120_000L)
        viewModel.togglePlayPause()
    }
}
