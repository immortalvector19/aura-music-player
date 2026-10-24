package com.aura.music.core.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.aura.music.core.database.TrackDao
import com.aura.music.core.database.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(
    private val context: Context,
    private val trackDao: TrackDao
) {

    suspend fun scanLocalTracks(): ScanResult = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        // Cache existing favorite track IDs to prevent data loss on library rescan
        val favoriteIds = trackDao.getFavoriteTrackIds().toSet()

        val scannedIds = mutableSetOf<Long>()
        val batch = mutableListOf<TrackEntity>()
        var totalScanned = 0

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackNumberColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val mimeTypeColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                scannedIds.add(id)

                val mediaUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()

                val albumId = c.getLong(albumIdColumn)
                val albumArtUri = if (albumId > 0) {
                    ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()
                } else null

                val title = c.getString(titleColumn) ?: "Unknown Track"
                val artist = c.getString(artistColumn) ?: "<Unknown Artist>"
                val album = c.getString(albumColumn) ?: "<Unknown Album>"
                val durationMs = c.getLong(durationColumn)
                val trackNumber = c.getInt(trackNumberColumn)
                val year = c.getInt(yearColumn)
                val mimeType = c.getString(mimeTypeColumn) ?: "audio/mpeg"
                val sizeBytes = c.getLong(sizeColumn)
                val dateAddedSeconds = c.getLong(dateAddedColumn)
                val path = c.getString(dataColumn) ?: ""

                // Preserve user favorite state across rescans
                val isFavorite = id in favoriteIds

                batch.add(
                    TrackEntity(
                        id = id,
                        mediaUri = mediaUri,
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = durationMs,
                        trackNumber = trackNumber,
                        discNumber = 0,
                        year = year,
                        mimeType = mimeType,
                        sizeBytes = sizeBytes,
                        albumArtUri = albumArtUri,
                        isFavorite = isFavorite,
                        dateAddedMs = dateAddedSeconds * 1000L,
                        path = path
                    )
                )

                // Batch insert every 500 items to keep peak memory minimal for 10,000+ tracks
                if (batch.size >= 500) {
                    trackDao.insertTracks(batch)
                    totalScanned += batch.size
                    batch.clear()
                }
            }

            // Insert remainder
            if (batch.isNotEmpty()) {
                trackDao.insertTracks(batch)
                totalScanned += batch.size
                batch.clear()
            }
        }

        // Clean up tracks deleted from disk using chunked deletion that never exceeds SQLite limits
        if (scannedIds.isNotEmpty()) {
            trackDao.deleteOrphanedTracks(scannedIds)
        }

        ScanResult(
            scannedCount = totalScanned,
            isSuccess = true
        )
    }

    data class ScanResult(
        val scannedCount: Int,
        val isSuccess: Boolean
    )
}
