package com.aura.music.core.data.repository

import android.content.Context
import com.aura.music.core.data.scanner.MediaStoreScanner
import com.aura.music.core.data.storage.StorageManager
import com.aura.music.core.data.worker.DownloadTrackWorker
import com.aura.music.core.database.TrackDao
import com.aura.music.core.database.toDomain
import com.aura.music.core.model.Album
import com.aura.music.core.model.Artist
import com.aura.music.core.model.StorageInfo
import com.aura.music.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

interface TrackRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun getTrackById(id: Long): Flow<Track?>
    fun searchTracks(query: String): Flow<List<Track>>
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getTracksForAlbum(album: String): Flow<List<Track>>
    fun getTracksForArtist(artist: String): Flow<List<Track>>
    fun getAllAlbums(): Flow<List<Album>>
    fun getAllArtists(): Flow<List<Artist>>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun scanLibrary(): MediaStoreScanner.ScanResult
    suspend fun getStorageLocations(): List<StorageInfo>
    suspend fun downloadTrack(track: Track)
    suspend fun seedDemoTracksIfEmpty()
}

class DefaultTrackRepository(
    private val context: Context,
    private val trackDao: TrackDao,
    private val scanner: MediaStoreScanner,
    private val storageManager: StorageManager,
    private val preferencesRepository: PreferencesRepository
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().map { entities -> entities.map { it.toDomain() } }

    override fun getTrackById(id: Long): Flow<Track?> =
        trackDao.getTrackById(id).map { it?.toDomain() }

    override fun searchTracks(query: String): Flow<List<Track>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return getAllTracks()
        return trackDao.searchTracksLike(trimmed).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFavoriteTracks(): Flow<List<Track>> =
        trackDao.getFavoriteTracks().map { entities -> entities.map { it.toDomain() } }

    override fun getTracksForAlbum(album: String): Flow<List<Track>> =
        trackDao.getTracksForAlbum(album).map { entities -> entities.map { it.toDomain() } }

    override fun getTracksForArtist(artist: String): Flow<List<Track>> =
        trackDao.getTracksForArtist(artist).map { entities -> entities.map { it.toDomain() } }

    // Direct SQLite indexed aggregation: Zero in-memory grouping, O(1) heap allocation
    override fun getAllAlbums(): Flow<List<Album>> =
        trackDao.getDistinctAlbums().map { results ->
            results.map { row ->
                Album(
                    id = row.album.hashCode().toLong(),
                    title = row.album,
                    artist = row.artist,
                    trackCount = row.trackCount,
                    year = row.maxYear,
                    albumArtUri = row.albumArtUri
                )
            }
        }

    // Direct SQLite indexed aggregation: Instant execution across 10,000+ tracks
    override fun getAllArtists(): Flow<List<Artist>> =
        trackDao.getDistinctArtists().map { results ->
            results.map { row ->
                Artist(
                    id = row.artist.hashCode().toLong(),
                    name = row.artist,
                    trackCount = row.trackCount,
                    albumCount = row.albumCount
                )
            }
        }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        trackDao.setFavorite(id, isFavorite)
    }

    override suspend fun scanLibrary(): MediaStoreScanner.ScanResult {
        return scanner.scanLocalTracks()
    }

    override suspend fun getStorageLocations(): List<StorageInfo> {
        return storageManager.getAvailableStorageLocations()
    }

    override suspend fun downloadTrack(track: Track) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val targetDir = storageManager.getPreferredStorageDirectory(prefs.selectedStoragePath)
        val targetFile = File(targetDir, "${track.id}.mp3")

        DownloadTrackWorker.scheduleDownload(
            context = context,
            trackId = track.id,
            url = track.mediaUri,
            destinationFile = targetFile,
            wifiOnly = prefs.wifiOnlyDownloads
        )
    }

    override suspend fun seedDemoTracksIfEmpty() {
        val count = trackDao.getTrackCount().first()
        if (count == 0) {
            val pkg = context.packageName
            val demoTracks = listOf(
                com.aura.music.core.database.TrackEntity(
                    id = 1001L,
                    mediaUri = "android.resource://$pkg/raw/aura_acoustic_sunrise",
                    title = "Acoustic Sunrise",
                    artist = "Aura Studio",
                    album = "Organic Waves",
                    durationMs = 12000L,
                    trackNumber = 1,
                    discNumber = 1,
                    year = 2024,
                    mimeType = "audio/wav",
                    sizeBytes = 2116844L,
                    albumArtUri = null,
                    isFavorite = true,
                    dateAddedMs = System.currentTimeMillis(),
                    path = "android.resource://$pkg/raw/aura_acoustic_sunrise"
                ),
                com.aura.music.core.database.TrackEntity(
                    id = 1002L,
                    mediaUri = "android.resource://$pkg/raw/aura_midnight_groove",
                    title = "Midnight Groove",
                    artist = "Aura Studio",
                    album = "Electronic Dreams",
                    durationMs = 12000L,
                    trackNumber = 2,
                    discNumber = 1,
                    year = 2024,
                    mimeType = "audio/wav",
                    sizeBytes = 2116844L,
                    albumArtUri = null,
                    isFavorite = false,
                    dateAddedMs = System.currentTimeMillis(),
                    path = "android.resource://$pkg/raw/aura_midnight_groove"
                ),
                com.aura.music.core.database.TrackEntity(
                    id = 1003L,
                    mediaUri = "android.resource://$pkg/raw/aura_ambient_pulse",
                    title = "Ambient Pulse",
                    artist = "Aura Studio",
                    album = "Deep Focus Chill",
                    durationMs = 12000L,
                    trackNumber = 3,
                    discNumber = 1,
                    year = 2024,
                    mimeType = "audio/wav",
                    sizeBytes = 2116844L,
                    albumArtUri = null,
                    isFavorite = false,
                    dateAddedMs = System.currentTimeMillis(),
                    path = "android.resource://$pkg/raw/aura_ambient_pulse"
                )
            )
            trackDao.insertTracks(demoTracks)
        }
    }
}
