package com.aura.music.core.database

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "tracks_fts")
@Fts4(contentEntity = TrackEntity::class)
data class TrackFtsEntity(
    val title: String,
    val artist: String,
    val album: String
)
