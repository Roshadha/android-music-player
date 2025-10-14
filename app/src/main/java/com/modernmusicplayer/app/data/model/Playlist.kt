package com.modernmusicplayer.app.data.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val songs: List<Song> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true
)
