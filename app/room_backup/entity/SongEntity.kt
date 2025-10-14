package com.modernmusicplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String,
    val audioUrl: String,
    val duration: Long,
    val genre: String,
    val releaseYear: Int,
    val isFavorite: Boolean = false
)
