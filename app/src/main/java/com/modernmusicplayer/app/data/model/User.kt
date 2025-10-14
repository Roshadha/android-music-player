package com.modernmusicplayer.app.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val favoritesSongIds: List<String> = emptyList(),
    val playlists: List<String> = emptyList()
)
