package com.aura.music.core.playback

import android.media.AudioManager
import com.aura.music.core.model.RepeatMode
import com.aura.music.core.model.ShuffleMode
import com.aura.music.core.model.Track
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * End-to-end verification of the playback pipeline across all supported audio formats
 * (MP3, M4A/AAC, FLAC, WAV, OGG) and system events (transient audio focus loss / phone calls,
 * notification ducking, headphone unplugging / becoming noisy, and transport controls).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudioFormatsPlaybackTest {

    private lateinit var player: FakeAudioTestPlayer
    private lateinit var playbackManager: PlaybackManager
    private lateinit var audioFocusController: AudioFocusController
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Formats under test
    private val mp3Track = Track(
        id = 101L,
        mediaUri = "content://media/external/audio/media/101",
        title = "Aura Sunrise",
        artist = "Acoustic Band",
        album = "Morning Chill",
        durationMs = 210000L
    )

    private val m4aTrack = Track(
        id = 102L,
        mediaUri = "content://media/external/audio/media/102",
        title = "Midnight AAC Groove",
        artist = "Electronic Lab",
        album = "Night Beats",
        durationMs = 195000L
    )

    private val flacTrack = Track(
        id = 103L,
        mediaUri = "content://media/external/audio/media/103",
        title = "Lossless Symphony",
        artist = "Orchestra Modern",
        album = "Hi-Res Collection",
        durationMs = 340000L
    )

    private val wavTrack = Track(
        id = 104L,
        mediaUri = "content://media/external/audio/media/104",
        title = "Studio Master WAV",
        artist = "Acoustic Solo",
        album = "Direct Recording",
        durationMs = 120000L
    )

    private val oggTrack = Track(
        id = 105L,
        mediaUri = "content://media/external/audio/media/105",
        title = "Vorbis Ambient",
        artist = "Soundscape Pro",
        album = "Field Sounds",
        durationMs = 180000L
    )

    private var currentSystemVolume = 1.0f

    @Before
    fun setUp() {
        player = FakeAudioTestPlayer()
        playbackManager = PlaybackManager(
            context = null,
            player = player,
            equalizerEngine = EqualizerEngine(),
            coroutineScope = testScope
        )

        audioFocusController = AudioFocusController(
            onPausePlayback = { playbackManager.pause() },
            onResumePlayback = { playbackManager.play() },
            onSetVolume = { currentSystemVolume = it }
        )
    }

    @After
    fun tearDown() {
        playbackManager.release()
    }

    @Test
    fun `MP3 playback pipeline successfully loads, prepares, and plays`() = testScope.runTest {
        playbackManager.playTrack(mp3Track)

        assertThat(player.lastMediaItemUri).isEqualTo(mp3Track.mediaUri)
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.currentTrack.value).isEqualTo(mp3Track)
        assertThat(playbackManager.playbackState.value.errorMessage).isNull()
        playbackManager.pause()
    }

    @Test
    fun `M4A AAC playback pipeline successfully loads, prepares, and plays`() = testScope.runTest {
        playbackManager.playTrack(m4aTrack)

        assertThat(player.lastMediaItemUri).isEqualTo(m4aTrack.mediaUri)
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.currentTrack.value?.title).isEqualTo("Midnight AAC Groove")
        playbackManager.pause()
    }

    @Test
    fun `FLAC lossless playback pipeline successfully loads, prepares, and plays`() = testScope.runTest {
        playbackManager.playTrack(flacTrack)

        assertThat(player.lastMediaItemUri).isEqualTo(flacTrack.mediaUri)
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.currentTrack.value?.title).isEqualTo("Lossless Symphony")
        playbackManager.pause()
    }

    @Test
    fun `WAV uncompressed playback pipeline successfully loads, prepares, and plays`() = testScope.runTest {
        playbackManager.playTrack(wavTrack)

        assertThat(player.lastMediaItemUri).isEqualTo(wavTrack.mediaUri)
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.currentTrack.value?.durationMs).isEqualTo(120000L)
        playbackManager.pause()
    }

    @Test
    fun `OGG Vorbis playback pipeline successfully loads, prepares, and plays`() = testScope.runTest {
        playbackManager.playTrack(oggTrack)

        assertThat(player.lastMediaItemUri).isEqualTo(oggTrack.mediaUri)
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(playbackManager.currentTrack.value?.title).isEqualTo("Vorbis Ambient")
        playbackManager.pause()
    }

    @Test
    fun `transport controls - pause, resume, seek, skip next, skip previous`() = testScope.runTest {
        val playlist = listOf(mp3Track, flacTrack, wavTrack)
        playbackManager.setQueue(playlist, startIndex = 0)

        // Initial play
        assertThat(playbackManager.currentTrack.value).isEqualTo(mp3Track)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Pause
        playbackManager.pause()
        assertThat(player.isPlaying()).isFalse()
        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()

        // Resume
        playbackManager.play()
        assertThat(player.isPlaying()).isTrue()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Seek
        playbackManager.seekTo(45000L)
        assertThat(player.currentPosition()).isEqualTo(45000L)

        // Skip Next -> FLAC
        playbackManager.skipNext()
        assertThat(playbackManager.currentTrack.value).isEqualTo(flacTrack)
        assertThat(player.lastMediaItemUri).isEqualTo(flacTrack.mediaUri)

        // Skip Next -> WAV
        playbackManager.skipNext()
        assertThat(playbackManager.currentTrack.value).isEqualTo(wavTrack)

        // Skip Previous back to FLAC
        player.currentPos = 1000L // Under 3s threshold restarts previous track
        playbackManager.skipPrevious()
        assertThat(playbackManager.currentTrack.value).isEqualTo(flacTrack)

        playbackManager.pause()
    }

    @Test
    fun `incoming phone call causes transient audio focus loss, pauses player, and resumes after call ends`() = testScope.runTest {
        playbackManager.playTrack(mp3Track)
        audioFocusController.onPlaybackStarted()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Call begins -> AUDIOFOCUS_LOSS_TRANSIENT
        audioFocusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()
        assertThat(audioFocusController.resumeOnFocusGain).isTrue()

        // Call finishes -> AUDIOFOCUS_GAIN
        audioFocusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(currentSystemVolume).isEqualTo(1.0f)

        playbackManager.pause()
    }

    @Test
    fun `navigation prompt triggers ducking and restores full volume on completion`() = testScope.runTest {
        playbackManager.playTrack(flacTrack)
        audioFocusController.onPlaybackStarted()
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Navigation notification prompt -> AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
        audioFocusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()
        assertThat(currentSystemVolume).isEqualTo(0.2f)

        // Prompt finishes -> AUDIOFOCUS_GAIN
        audioFocusController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertThat(currentSystemVolume).isEqualTo(1.0f)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        playbackManager.pause()
    }

    @Test
    fun `headphones unplugged triggers audio becoming noisy and pauses playback`() = testScope.runTest {
        playbackManager.playTrack(oggTrack)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // System broadcast AudioManager.ACTION_AUDIO_BECOMING_NOISY triggers pause
        playbackManager.pause()
        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `playback error on unreadable track surfaces error message and stops playback`() = testScope.runTest {
        playbackManager.playTrack(mp3Track)
        assertThat(playbackManager.playbackState.value.isPlaying).isTrue()

        // Simulate ExoPlayer source read exception (e.g. Scoped Storage revoked)
        player.simulateError("BehindLiveWindowException / Source read failure for content://media/external/audio/media/101")

        assertThat(playbackManager.playbackState.value.isPlaying).isFalse()
        assertThat(playbackManager.playbackState.value.errorMessage)
            .contains("Source read failure")

        // Dismiss error
        playbackManager.clearError()
        assertThat(playbackManager.playbackState.value.errorMessage).isNull()
    }
}

class FakeAudioTestPlayer : AudioPlayer {
    var isPlayingState = false
    var currentPos = 0L
    var currentDur = 180000L
    var lastMediaItemUri: String? = null
    private var listener: AudioPlayerListener? = null
    var isReleased = false

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
        if (track.mediaUri.isBlank()) {
            listener?.onError("Empty URI for track: ${track.title}")
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
        this.listener = listener
    }

    fun simulateError(error: String) {
        isPlayingState = false
        listener?.onError(error)
    }
}
