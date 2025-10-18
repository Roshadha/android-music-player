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
}
