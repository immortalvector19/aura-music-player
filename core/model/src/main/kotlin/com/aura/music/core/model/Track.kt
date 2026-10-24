package com.aura.music.core.model

/**
 * Immutable domain representation of a playable audio asset.
 */
data class Track(
    val id: Long,
    val mediaUri: String,
    val title: String,
    val artist: String = "<Unknown Artist>",
    val album: String = "<Unknown Album>",
    val durationMs: Long = 0L,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val year: Int = 0,
    val mimeType: String = "audio/mpeg",
    val sizeBytes: Long = 0L,
    val albumArtUri: String? = null,
    val isFavorite: Boolean = false,
    val dateAddedMs: Long = 0L,
    val path: String = ""
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "00:00"
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val remainingSeconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
            } else {
                String.format("%02d:%02d", minutes, remainingSeconds)
            }
        }
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val trackCount: Int = 0,
    val year: Int = 0,
    val albumArtUri: String? = null
)

data class Artist(
    val id: Long,
    val name: String,
    val trackCount: Int = 0,
    val albumCount: Int = 0
)

data class Playlist(
    val id: Long,
    val name: String,
    val trackCount: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis()
)
