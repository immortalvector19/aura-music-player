package com.aura.music.core.playback

import com.aura.music.core.model.Track
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FakeErrorCapableAudioPlayer : AudioPlayer {
    var isPlayingState = false
    var currentPos = 0L
    var currentDur = 60000L
    var lastMediaItemUri: String? = null
    var playerListener: AudioPlayerListener? = null
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
        if (track.mediaUri.isBlank()) {
            playerListener?.onError("Invalid audio URI: URI cannot be empty")
            return
        }
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

    fun simulateError(message: String) {
        isPlayingState = false
        playerListener?.onError(message)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackErrorRegressionTest {

    private lateinit var player: FakeErrorCapableAudioPlayer
    private lateinit var playbackManager: PlaybackManager
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val validTrack = Track(
        id = 1001L,
        mediaUri = "content://media/external/audio/media/1001",
        title = "Valid Track",
        artist = "Test Artist",
        durationMs = 180000L
    )

    private val invalidTrack = Track(
        id = 1002L,
        mediaUri = "", // Empty / unresolvable URI
        title = "Corrupted Track",
        artist = "Test Artist",
        durationMs = 0L
    )

    @Before
    fun setUp() {
        player = FakeErrorCapableAudioPlayer()
        playbackManager = PlaybackManager(
            context = null,
            player = player,
            equalizerEngine = EqualizerEngine(),
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        playbackManager.release()
    }

    @Test
    fun `when player encounters playback error, playbackState surfaces errorMessage to UI`() = testScope.runTest {
        playbackManager.playTrack(validTrack)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Trigger audio playback error (e.g. Scoped Storage / permission / codec failure)
        player.simulateError("Source error: cannot open content://media/external/audio/media/1001")

        // BUG SYMPTOM: In unpatched code, errorMessage is never updated and remains null
        assertThat(playbackManager.playbackState.value.errorMessage)
            .isEqualTo("Source error: cannot open content://media/external/audio/media/1001")
        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()

        // Test clear error functionality
        playbackManager.clearError()
        assertThat(playbackManager.playbackState.value.errorMessage).isNull()
    }

    @Test
    fun `when playing a new track after an error, previous errorMessage is cleared`() = testScope.runTest {
        playbackManager.playTrack(validTrack)
        player.simulateError("Previous error")

        // Play next track
        playbackManager.playTrack(validTrack)

        // Previous error must be cleared
        assertThat(playbackManager.playbackState.value.errorMessage).isNull()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        playbackManager.pause()
    }
}
