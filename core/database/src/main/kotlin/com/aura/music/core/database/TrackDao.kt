package com.aura.music.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class AlbumQueryResult(
    val album: String,
    val artist: String,
    val trackCount: Int,
    val maxYear: Int,
    val albumArtUri: String?
)

data class ArtistQueryResult(
    val artist: String,
    val trackCount: Int,
    val albumCount: Int
)

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: Long): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT id FROM tracks WHERE isFavorite = 1")
    suspend fun getFavoriteTrackIds(): List<Long>

    @Query("SELECT id FROM tracks")
    suspend fun getAllTrackIds(): List<Long>

    @Query("""
        SELECT t.* FROM tracks t
        JOIN tracks_fts fts ON t.id = fts.rowid
        WHERE tracks_fts MATCH :query || '*'
        ORDER BY t.title COLLATE NOCASE ASC
    """)
    fun searchTracksFts(query: String): Flow<List<TrackEntity>>

    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%' 
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun searchTracksLike(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY trackNumber ASC, title ASC")
    fun getTracksForAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album ASC, trackNumber ASC, title ASC")
    fun getTracksForArtist(artist: String): Flow<List<TrackEntity>>

    @Query("""
        SELECT album, artist, COUNT(*) as trackCount, MAX(year) as maxYear, MAX(albumArtUri) as albumArtUri
        FROM tracks
        GROUP BY album
        ORDER BY album COLLATE NOCASE ASC
    """)
    fun getDistinctAlbums(): Flow<List<AlbumQueryResult>>

    @Query("""
        SELECT artist, COUNT(*) as trackCount, COUNT(DISTINCT album) as albumCount
        FROM tracks
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE ASC
    """)
    fun getDistinctArtists(): Flow<List<ArtistQueryResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE tracks SET mediaUri = :mediaUri WHERE id = :id")
    suspend fun updateTrackMediaUri(id: Long, mediaUri: String)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)

    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun deleteTracksByIds(ids: List<Long>)

    @Transaction
    suspend fun deleteOrphanedTracks(validScannedIds: Set<Long>) {
        val existingIds = getAllTrackIds()
        val orphans = existingIds.filterNot { it in validScannedIds }
        // Chunk to 500 to never exceed SQLite 999 parameter limit on budget Android devices
        orphans.chunked(500).forEach { chunk ->
            deleteTracksByIds(chunk)
        }
    }

    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>
}
