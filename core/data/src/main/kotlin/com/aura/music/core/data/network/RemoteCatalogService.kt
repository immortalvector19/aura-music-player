package com.aura.music.core.data.network

import com.aura.music.core.model.Track
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

data class RemoteTrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album") val album: String,
    @SerializedName("duration_seconds") val durationSeconds: Long,
    @SerializedName("stream_url") val streamUrl: String,
    @SerializedName("cover_art_url") val coverArtUrl: String?,
    @SerializedName("size_bytes") val sizeBytes: Long?
) {
    fun toDomain(): Track = Track(
        id = id,
        mediaUri = streamUrl,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationSeconds * 1000L,
        sizeBytes = sizeBytes ?: 0L,
        albumArtUri = coverArtUrl,
        isFavorite = false
    )
}

interface RemoteCatalogApi {
    @GET("catalog/featured.json")
    suspend fun getFeaturedTracks(): List<RemoteTrackDto>
}
