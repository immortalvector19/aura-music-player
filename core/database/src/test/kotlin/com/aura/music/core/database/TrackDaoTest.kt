package com.aura.music.core.database

import android.content.Context
import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrackDaoTest {

    private lateinit var database: AuraDatabase
    private lateinit var trackDao: TrackDao

    @Before
    fun createDb() {
        val context: Context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        trackDao = database.trackDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndQueryTracks() = runTest {
        val tracks = listOf(
            createTestEntity(1L, "Solaris", "Photay", "Onism"),
            createTestEntity(2L, "Aura", "Bicep", "Bicep")
        )

        trackDao.insertTracks(tracks)
        val all = trackDao.getAllTracks().first()

        assertThat(all).hasSize(2)
        assertThat(all[0].title).isEqualTo("Aura") // Sorted NOCASE ASC
        assertThat(all[1].title).isEqualTo("Solaris")
    }

    @Test
    fun deleteOrphanedTracksHandlesOver999ItemsWithoutCrashing() = runTest {
        // Create 1,500 tracks (exceeding SQLite's 999 bind variable limit)
        val tracks = (1L..1500L).map { id ->
            createTestEntity(id, "Track $id", "Artist", "Album")
        }
        trackDao.insertTracks(tracks)

        assertThat(trackDao.getAllTrackIds()).hasSize(1500)

        // Keep only IDs 1..300, orphaning 1,200 tracks
        val validIds = (1L..300L).toSet()
        trackDao.deleteOrphanedTracks(validIds)

        val remainingIds = trackDao.getAllTrackIds()
        assertThat(remainingIds).hasSize(300)
        assertThat(remainingIds).containsExactlyElementsIn(validIds)
    }

    @Test
    fun distinctAlbumsAndArtistsAggregateCorrectlyInSql() = runTest {
        val tracks = listOf(
            createTestEntity(1L, "Song 1", "Artist A", "Album Alpha"),
            createTestEntity(2L, "Song 2", "Artist A", "Album Alpha"),
            createTestEntity(3L, "Song 3", "Artist A", "Album Beta"),
            createTestEntity(4L, "Song 4", "Artist B", "Album Gamma")
        )
        trackDao.insertTracks(tracks)

        val albums = trackDao.getDistinctAlbums().first()
        assertThat(albums).hasSize(3)
        val albumAlpha = albums.first { it.album == "Album Alpha" }
        assertThat(albumAlpha.trackCount).isEqualTo(2)
        assertThat(albumAlpha.artist).isEqualTo("Artist A")

        val artists = trackDao.getDistinctArtists().first()
        assertThat(artists).hasSize(2)
        val artistA = artists.first { it.artist == "Artist A" }
        assertThat(artistA.trackCount).isEqualTo(3)
        assertThat(artistA.albumCount).isEqualTo(2)
    }

    private fun createTestEntity(
        id: Long,
        title: String,
        artist: String,
        album: String
    ): TrackEntity = TrackEntity(
        id = id,
        mediaUri = "content://audio/$id",
        title = title,
        artist = artist,
        album = album,
        durationMs = 200000L,
        trackNumber = 1,
        discNumber = 1,
        year = 2024,
        mimeType = "audio/mp3",
        sizeBytes = 5000000L,
        albumArtUri = "content://art/$id",
        isFavorite = false,
        dateAddedMs = 1700000000L,
        path = "/storage/emulated/0/Music/$title.mp3"
    )
}
