package com.modernmusicplayer.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtUrl: String = "",
    val audioUrl: String = "",
    val duration: Long = 0L, // in milliseconds
    val genre: String = "",
    val releaseYear: Int = 0,
    var isFavorite: Boolean = false,
    val isLocal: Boolean = false
) : Parcelable

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
