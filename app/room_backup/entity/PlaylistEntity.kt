package com.modernmusicplayer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverImageUrl: String,
    val songIds: List<String>,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
