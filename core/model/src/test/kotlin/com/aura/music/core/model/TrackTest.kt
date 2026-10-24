package com.aura.music.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TrackTest {

    @Test
    fun `formattedDuration converts milliseconds to mm ss string correctly`() {
        val track = Track(
            id = 1L,
            mediaUri = "content://media/external/audio/media/1",
            title = "Midnight Horizon",
            artist = "Aura Ensemble",
            album = "Cosmic Echoes",
            durationMs = 215_000L, // 3m 35s
            albumArtUri = "content://media/external/audio/albumart/10",
            path = "/storage/emulated/0/Music/midnight_horizon.mp3",
            sizeBytes = 8_400_000L
        )

        assertThat(track.formattedDuration).isEqualTo("03:35")

        val longTrack = track.copy(durationMs = 3_725_000L) // 1h 2m 5s
        assertThat(longTrack.formattedDuration).isEqualTo("1:02:05")
    }

    @Test
    fun `queue nextTrack advances index and handles repeat modes`() {
        val track1 = Track(1L, "uri1", "Song 1", "Artist", "Album", 100_000L)
        val track2 = Track(2L, "uri2", "Song 2", "Artist", "Album", 120_000L)
        val queue = Queue(tracks = listOf(track1, track2), currentIndex = 0, repeatMode = RepeatMode.ALL)

        val nextQueue = queue.next()
        assertThat(nextQueue.currentIndex).isEqualTo(1)
        assertThat(nextQueue.currentTrack).isEqualTo(track2)

        val wrappedQueue = nextQueue.next()
        assertThat(wrappedQueue.currentIndex).isEqualTo(0)
        assertThat(wrappedQueue.currentTrack).isEqualTo(track1)
    }

    @Test
    fun `equalizerBand clampLevel ensures values stay within bounds`() {
        val band = EqualizerBand(
            index = 0,
            centerFreqHz = 60,
            minLevelMb = -1500,
            maxLevelMb = 1500,
            currentLevelMb = 0
        )

        val clampedHigh = band.withLevel(2000)
        assertThat(clampedHigh.currentLevelMb).isEqualTo(1500)

        val clampedLow = band.withLevel(-3000)
        assertThat(clampedLow.currentLevelMb).isEqualTo(-1500)
    }

    @Test
    fun `storageInfo correctly identifies low storage when available is below 500MB`() {
        val healthyStorage = StorageInfo(
            totalBytes = 64_000_000_000L,
            availableBytes = 5_000_000_000L,
            isExternalSd = false,
            storagePath = "/storage/emulated/0"
        )
        assertThat(healthyStorage.isLowStorage).isFalse()

        val lowStorage = StorageInfo(
            totalBytes = 64_000_000_000L,
            availableBytes = 400_000_000L, // 400MB < 500MB
            isExternalSd = false,
            storagePath = "/storage/emulated/0"
        )
        assertThat(lowStorage.isLowStorage).isTrue()
    }
}
