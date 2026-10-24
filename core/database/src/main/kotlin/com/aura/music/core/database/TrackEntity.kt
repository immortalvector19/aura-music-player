package com.aura.music.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aura.music.core.model.Track

@Entity(
    tableName = "tracks",
    indices = [
        Index("title"),
        Index("artist"),
        Index("album"),
        Index("dateAddedMs"),
        Index("isFavorite"),
        Index("path")
    ]
)
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val mediaUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val year: Int,
    val mimeType: String,
    val sizeBytes: Long,
    val albumArtUri: String?,
    val isFavorite: Boolean,
    val dateAddedMs: Long,
    val path: String
)

fun TrackEntity.toDomain(): Track = Track(
    id = id,
    mediaUri = mediaUri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    trackNumber = trackNumber,
    discNumber = discNumber,
    year = year,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    albumArtUri = albumArtUri,
    isFavorite = isFavorite,
    dateAddedMs = dateAddedMs,
    path = path
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    mediaUri = mediaUri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    trackNumber = trackNumber,
    discNumber = discNumber,
    year = year,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    albumArtUri = albumArtUri,
    isFavorite = isFavorite,
    dateAddedMs = dateAddedMs,
    path = path
)
