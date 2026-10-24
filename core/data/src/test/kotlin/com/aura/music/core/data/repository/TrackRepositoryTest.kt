package com.aura.music.core.data.repository

import android.content.Context
import com.aura.music.core.data.scanner.MediaStoreScanner
import com.aura.music.core.data.storage.StorageManager
import com.aura.music.core.database.AlbumQueryResult
import com.aura.music.core.database.ArtistQueryResult
import com.aura.music.core.database.TrackDao
import com.aura.music.core.database.TrackEntity
import com.aura.music.core.model.StorageInfo
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TrackRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val trackDao = mockk<TrackDao>()
    private val scanner = mockk<MediaStoreScanner>()
    private val storageManager = mockk<StorageManager>()
    private val preferencesRepository = mockk<PreferencesRepository>()

    private lateinit var repository: DefaultTrackRepository

    @Before
    fun setUp() {
        repository = DefaultTrackRepository(
            context = context,
            trackDao = trackDao,
            scanner = scanner,
            storageManager = storageManager,
            preferencesRepository = preferencesRepository
        )
    }

    @Test
    fun `getAllTracks maps track entities into domain models properly`() = runTest {
        val entities = listOf(
            TrackEntity(
                id = 1L,
                mediaUri = "content://audio/1",
                title = "Dawn Chorus",
                artist = "Boards of Canada",
                album = "Geogaddi",
                durationMs = 235000L,
                trackNumber = 1,
                discNumber = 1,
                year = 2002,
                mimeType = "audio/flac",
                sizeBytes = 15000000L,
                albumArtUri = "content://art/1",
                isFavorite = true,
                dateAddedMs = 1700000000L,
                path = "/storage/0/Music/dawn.flac"
            )
        )
        every { trackDao.getAllTracks() } returns flowOf(entities)

        val result = repository.getAllTracks().first()

        assertThat(result).hasSize(1)
        assertThat(result.first().title).isEqualTo("Dawn Chorus")
        assertThat(result.first().artist).isEqualTo("Boards of Canada")
        assertThat(result.first().isFavorite).isTrue()
    }

    @Test
    fun `getTrackById returns reactive domain track`() = runTest {
        val entity = TrackEntity(1L, "uri1", "Song 1", "Artist", "Album", 100000L, 1, 0, 2024, "audio/mp3", 1000L, null, false, 0L, "/path")
        every { trackDao.getTrackById(1L) } returns flowOf(entity)

        val track = repository.getTrackById(1L).first()
        assertThat(track).isNotNull()
        assertThat(track?.title).isEqualTo("Song 1")
    }

    @Test
    fun `getAllAlbums leverages direct SQL aggregation results`() = runTest {
        val queryResults = listOf(
            AlbumQueryResult("Album One", "Artist A", 10, 2024, "art1"),
            AlbumQueryResult("Album Two", "Artist B", 8, 2023, "art2")
        )
        every { trackDao.getDistinctAlbums() } returns flowOf(queryResults)

        val albums = repository.getAllAlbums().first()

        assertThat(albums).hasSize(2)
        assertThat(albums[0].title).isEqualTo("Album One")
        assertThat(albums[0].trackCount).isEqualTo(10)
        assertThat(albums[1].title).isEqualTo("Album Two")
        assertThat(albums[1].trackCount).isEqualTo(8)
    }

    @Test
    fun `getAllArtists leverages direct SQL aggregation results`() = runTest {
        val queryResults = listOf(
            ArtistQueryResult("Artist A", 15, 2),
            ArtistQueryResult("Artist B", 7, 1)
        )
        every { trackDao.getDistinctArtists() } returns flowOf(queryResults)

        val artists = repository.getAllArtists().first()

        assertThat(artists).hasSize(2)
        assertThat(artists[0].name).isEqualTo("Artist A")
        assertThat(artists[0].trackCount).isEqualTo(15)
        assertThat(artists[0].albumCount).isEqualTo(2)
    }

    @Test
    fun `getStorageLocations dispatches and returns storage info`() = runTest {
        val locations = listOf(
            StorageInfo(64000000000L, 10000000000L, false, "/storage/emulated/0")
        )
        coEvery { storageManager.getAvailableStorageLocations() } returns locations

        val result = repository.getStorageLocations()
        assertThat(result).hasSize(1)
        assertThat(result.first().isLowStorage).isFalse()
    }

    @Test
    fun `seedDemoTracksIfEmpty inserts demo tracks when database count is zero`() = runTest {
        every { trackDao.getTrackCount() } returns flowOf(0)
        coEvery { trackDao.insertTracks(any()) } returns Unit
        every { context.packageName } returns "com.aura.music"

        repository.seedDemoTracksIfEmpty()

        io.mockk.coVerify(exactly = 1) { trackDao.insertTracks(match { it.size == 3 }) }
    }
}
