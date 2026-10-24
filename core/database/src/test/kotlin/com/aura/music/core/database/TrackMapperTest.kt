package com.aura.music.core.database

import com.aura.music.core.model.Track
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TrackMapperTest {

    @Test
    fun `domain track to entity and back preserves all audio metadata fields`() {
        val originalTrack = Track(
            id = 42L,
            mediaUri = "content://media/external/audio/media/42",
            title = "Starlight Frequency",
            artist = "Aura Sound Lab",
            album = "Nebula Sessions",
            durationMs = 245000L,
            trackNumber = 3,
            discNumber = 1,
            year = 2024,
            mimeType = "audio/flac",
            sizeBytes = 25000000L,
            albumArtUri = "content://media/external/audio/albumart/5",
            isFavorite = true,
            dateAddedMs = 1700000000000L,
            path = "/storage/emulated/0/Music/starlight.flac"
        )

        val entity = originalTrack.toEntity()
        assertThat(entity.id).isEqualTo(42L)
        assertThat(entity.title).isEqualTo("Starlight Frequency")
        assertThat(entity.artist).isEqualTo("Aura Sound Lab")
        assertThat(entity.isFavorite).isTrue()
        assertThat(entity.mimeType).isEqualTo("audio/flac")

        val convertedBack = entity.toDomain()
        assertThat(convertedBack).isEqualTo(originalTrack)
    }

    @Test
    fun `track entity handles null optional values gracefully`() {
        val entity = TrackEntity(
            id = 100L,
            mediaUri = "content://media/external/audio/media/100",
            title = "Acoustic Solo",
            artist = "Soloist",
            album = "Unplugged",
            durationMs = 180000L,
            trackNumber = 1,
            discNumber = 0,
            year = 2023,
            mimeType = "audio/mp3",
            sizeBytes = 5000000L,
            albumArtUri = null,
            isFavorite = false,
            dateAddedMs = 1690000000000L,
            path = "/storage/emulated/0/Music/acoustic.mp3"
        )

        val domain = entity.toDomain()
        assertThat(domain.albumArtUri).isNull()
        assertThat(domain.formattedDuration).isEqualTo("03:00")
        assertThat(domain.isFavorite).isFalse()
    }
}
