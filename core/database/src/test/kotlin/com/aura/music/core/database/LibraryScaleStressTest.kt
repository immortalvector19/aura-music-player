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
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryScaleStressTest {

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
    fun tenThousandTracksInsertionAndQueryPerformance() = runTest {
        val count = 10_000
        val tracks = (1L..count.toLong()).map { id ->
            TrackEntity(
                id = id,
                mediaUri = "content://media/external/audio/media/$id",
                title = "Track Title ${id.toString().padStart(5, '0')}",
                artist = "Artist ${id % 150}",
                album = "Album ${id % 500}",
                durationMs = 210000L,
                trackNumber = (id % 12).toInt() + 1,
                discNumber = 1,
                year = 2020 + (id % 4).toInt(),
                mimeType = "audio/mpeg",
                sizeBytes = 8_000_000L,
                albumArtUri = null,
                isFavorite = id % 7 == 0L,
                dateAddedMs = 1700000000L + id,
                path = "/storage/emulated/0/Music/track_$id.mp3"
            )
        }

        // Insert in chunks of 1,000
        val insertDuration = measureTimeMillis {
            tracks.chunked(1000).forEach { chunk ->
                trackDao.insertTracks(chunk)
            }
        }
        println("Inserted 10,000 tracks in ${insertDuration}ms")

        // Verify count
        val totalCount = trackDao.getTrackCount().first()
        assertThat(totalCount).isEqualTo(10_000)

        // Query all tracks sorted
        val queryDuration = measureTimeMillis {
            val allTracks = trackDao.getAllTracks().first()
            assertThat(allTracks).hasSize(10_000)
            assertThat(allTracks.first().title).isEqualTo("Track Title 00001")
        }
        println("Queried 10,000 sorted tracks in ${queryDuration}ms")

        // Grouped queries (distinct albums)
        val albumDuration = measureTimeMillis {
            val albums = trackDao.getDistinctAlbums().first()
            assertThat(albums).isNotEmpty()
        }
        println("Grouped distinct albums query in ${albumDuration}ms")

        // Search query
        val searchDuration = measureTimeMillis {
            val searchResults = trackDao.searchTracksLike("00500").first()
            assertThat(searchResults).isNotEmpty()
        }
        println("Search query across 10,000 tracks in ${searchDuration}ms")
    }
}
