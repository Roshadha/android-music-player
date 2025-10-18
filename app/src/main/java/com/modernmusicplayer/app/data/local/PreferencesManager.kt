package com.modernmusicplayer.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "music_player_prefs",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_FAVORITES = "favorite_song_ids"
        private const val KEY_RECENT_TRACKS = "recent_tracks"
        private const val KEY_PLAYLISTS = "user_playlists"
        private const val MAX_RECENT_TRACKS = 50
    }
    
    // Favorites management
    fun addFavorite(songId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(songId)
        saveFavorites(favorites)
    }
    
    fun removeFavorite(songId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(songId)
        saveFavorites(favorites)
    }
    
    fun isFavorite(songId: String): Boolean {
        return getFavorites().contains(songId)
    }
    
    fun toggleFavorite(songId: String): Boolean {
        return if (isFavorite(songId)) {
            removeFavorite(songId)
            false
        } else {
            addFavorite(songId)
            true
        }
    }
    
    fun getFavorites(): Set<String> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type)
    }
    
    private fun saveFavorites(favorites: Set<String>) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(favorites)).apply()
    }
    
    // Recently played management
    data class RecentTrack(
        val songId: String,
        val timestamp: Long
    )
    
    fun addRecentTrack(songId: String) {
        val recentTracks = getRecentTracks().toMutableList()
        
        // Remove if already exists
        recentTracks.removeAll { it.songId == songId }
        
        // Add at the beginning
        recentTracks.add(0, RecentTrack(songId, System.currentTimeMillis()))
        
        // Keep only MAX_RECENT_TRACKS
        if (recentTracks.size > MAX_RECENT_TRACKS) {
            recentTracks.subList(MAX_RECENT_TRACKS, recentTracks.size).clear()
        }
        
        saveRecentTracks(recentTracks)
    }
    
    fun getRecentTracks(): List<RecentTrack> {
        val json = prefs.getString(KEY_RECENT_TRACKS, null) ?: return emptyList()
        val type = object : TypeToken<List<RecentTrack>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun getRecentTrackIds(): List<String> {
        return getRecentTracks().map { it.songId }
    }
    
    private fun saveRecentTracks(tracks: List<RecentTrack>) {
        prefs.edit().putString(KEY_RECENT_TRACKS, gson.toJson(tracks)).apply()
    }
    
    fun clearRecentTracks() {
        prefs.edit().remove(KEY_RECENT_TRACKS).apply()
    }
    
    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }
    
    // Playlist management
    data class PlaylistData(
        val id: String,
        val name: String,
        val description: String = "",
        val songIds: List<String> = emptyList(),
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
    
    fun createPlaylist(name: String, description: String = ""): PlaylistData {
        val playlists = getPlaylists().toMutableList()
        val newPlaylist = PlaylistData(
            id = "playlist_${System.currentTimeMillis()}",
            name = name,
            description = description,
            songIds = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlists.add(newPlaylist)
        savePlaylists(playlists)
        return newPlaylist
    }
    
    fun updatePlaylist(playlistId: String, name: String? = null, description: String? = null) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = playlists[index]
            playlists[index] = playlist.copy(
                name = name ?: playlist.name,
                description = description ?: playlist.description,
                updatedAt = System.currentTimeMillis()
            )
            savePlaylists(playlists)
        }
    }
    
    fun deletePlaylist(playlistId: String) {
        val playlists = getPlaylists().toMutableList()
        playlists.removeAll { it.id == playlistId }
        savePlaylists(playlists)
    }
    
    fun addSongToPlaylist(playlistId: String, songId: String) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = playlists[index]
            if (!playlist.songIds.contains(songId)) {
                val updatedSongIds = playlist.songIds.toMutableList()
                updatedSongIds.add(songId)
                playlists[index] = playlist.copy(
                    songIds = updatedSongIds,
                    updatedAt = System.currentTimeMillis()
                )
                savePlaylists(playlists)
            }
        }
    }
    
    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = playlists[index]
            val updatedSongIds = playlist.songIds.toMutableList()
            updatedSongIds.remove(songId)
            playlists[index] = playlist.copy(
                songIds = updatedSongIds,
                updatedAt = System.currentTimeMillis()
            )
            savePlaylists(playlists)
        }
    }
    
    fun getPlaylists(): List<PlaylistData> {
        val json = prefs.getString(KEY_PLAYLISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<PlaylistData>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun getPlaylist(playlistId: String): PlaylistData? {
        return getPlaylists().find { it.id == playlistId }
    }
    
    private fun savePlaylists(playlists: List<PlaylistData>) {
        prefs.edit().putString(KEY_PLAYLISTS, gson.toJson(playlists)).apply()
    }
    
    fun clearPlaylists() {
        prefs.edit().remove(KEY_PLAYLISTS).apply()
    }
}
