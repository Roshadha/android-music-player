package com.modernmusicplayer.app.data.repository

import android.content.Context
import android.util.Log
import com.modernmusicplayer.app.data.api.ApiClient
import com.modernmusicplayer.app.data.api.DeezerTrack
import com.modernmusicplayer.app.data.api.JioSaavnSong
import com.modernmusicplayer.app.data.local.PreferencesManager
import com.modernmusicplayer.app.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository(private val context: Context) {
    
    private val deezerApi = ApiClient.deezerApi
    private val jioSaavnApi = ApiClient.jioSaavnApi
    private val jamendoApi = ApiClient.jamendoApi
    private val pipedApi = ApiClient.pipedApi
    private val youtubeApi = ApiClient.youtubeApi
    private val freeMusicApi = ApiClient.freeMusicApi
    private val musicBrainzApi = ApiClient.musicBrainzApi
    private val itunesApi = ApiClient.itunesApi
    private val prefsManager = PreferencesManager(context)
    private val cachedSongs = mutableListOf<Song>()
    private val localMusicCache = mutableListOf<Song>()
    private var isInitialized = false
    
    suspend fun getGlobalSongs(): Flow<List<Song>> = flow {
        if (!isInitialized || cachedSongs.isEmpty()) {
            loadGlobalMusic()
        }
        emit(cachedSongs)
    }
    
    private suspend fun loadGlobalMusic() {
        try {
            val allSongs = mutableListOf<Song>()
            
            Log.e("MusicRepository", "========================================")
            Log.e("MusicRepository", "LOADING MUSIC COLLECTION")
            Log.e("MusicRepository", "========================================")
            
            // Load curated collection (30 songs - instant, always works)
            allSongs.addAll(getCuratedMusicCollection())
            Log.e("MusicRepository", "✓✓✓ LOADED ${allSongs.size} SONGS ✓✓✓")
            
            Log.e("MusicRepository", "✓✓✓ TOTAL LOADED: ${allSongs.size} SONGS ✓✓✓")
            
            if (allSongs.isNotEmpty()) {
                cachedSongs.clear()
                cachedSongs.addAll(allSongs.distinctBy { it.id })
                isInitialized = true
                Log.d("MusicRepository", "Final cached songs: ${cachedSongs.size}")
            } else {
                // Fallback
                Log.w("MusicRepository", "No songs loaded, using curated collection")
                cachedSongs.addAll(getCuratedMusicCollection())
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to load music: ${e.message}")
            if (cachedSongs.isEmpty()) {
                cachedSongs.addAll(getCuratedMusicCollection())
            }
        }
    }
    
    private fun DeezerTrack.toSong(): Song {
        return Song(
            id = id.toString(),
            title = title,
            artist = artist.name,
            album = album.title,
            albumArtUrl = album.cover_big ?: album.cover_medium ?: album.cover ?: "",
            audioUrl = preview,
            duration = (duration * 1000).toLong(),
            genre = "Global",
            releaseYear = 2024
        )
    }
    
    private fun JioSaavnSong.toSong(): Song? {
        val downloadUrl = downloadUrl?.firstOrNull()?.link ?: url ?: return null
        val songTitle = title ?: name ?: return null
        
        return Song(
            id = id,
            title = songTitle,
            artist = artists ?: subtitle ?: "Unknown Artist",
            album = album ?: "Single",
            albumArtUrl = image?.replace("150x150", "500x500") ?: "",
            audioUrl = downloadUrl,
            duration = duration?.toLongOrNull()?.times(1000) ?: 180000,
            genre = language ?: "Global",
            releaseYear = year?.toIntOrNull() ?: 2024
        )
    }
    
    private fun com.modernmusicplayer.app.data.api.JamendoTrack.toSong(): Song {
        return Song(
            id = "jamendo_$id",
            title = name,
            artist = artist_name,
            album = album_name,
            albumArtUrl = album_image,
            audioUrl = audio, // Full song URL!
            duration = (duration * 1000).toLong(),
            genre = "Free Music",
            releaseYear = releasedate?.take(4)?.toIntOrNull() ?: 2024
        )
    }
    
    private fun com.modernmusicplayer.app.data.api.MusicBrainzRecording.toSong(): Song? {
        val artistName = artistCredit?.firstOrNull()?.name ?: "Unknown Artist"
        val albumName = releases?.firstOrNull()?.title ?: "Single"
        val duration = length ?: 180000 // Default to 3 minutes if no duration
        val releaseDate = releases?.firstOrNull()?.date
        val year = releaseDate?.take(4)?.toIntOrNull() ?: 2024
        val genreName = genres?.firstOrNull()?.name ?: tags?.firstOrNull()?.name ?: "Unknown"
        
        // Get YouTube URL from relations if available
        val youtubeUrl = relations?.find { 
            it.type == "youtube" && it.url?.resource?.contains("youtube.com") == true 
        }?.url?.resource
        
        // Try to find cover art from release
        val coverArtUrl = releases?.firstOrNull()?.let { release ->
            "https://coverartarchive.org/release/${release.id}/front-250"
        } ?: "https://picsum.photos/seed/mb$id/500"
        
        return Song(
            id = "mb_$id",
            title = title,
            artist = artistName,
            album = albumName,
            albumArtUrl = coverArtUrl,
            audioUrl = youtubeUrl ?: "",
            duration = duration.toLong(),
            genre = genreName,
            releaseYear = year
        )
    }
    
    private fun com.modernmusicplayer.app.data.api.ITunesTrack.toSong(): Song {
        return Song(
            id = "itunes_${trackId}",
            title = trackName,
            artist = artistName,
            album = collectionName ?: "Single",
            albumArtUrl = artworkUrl100 ?: artworkUrl60 ?: "https://picsum.photos/seed/itunes$trackId/500",
            audioUrl = previewUrl ?: "",
            duration = trackTimeMillis ?: 30000,
            genre = primaryGenreName ?: "Music",
            releaseYear = releaseDate?.take(4)?.toIntOrNull() ?: 2024
        )
    }
    
    private suspend fun com.modernmusicplayer.app.data.api.YouTubeSearchItem.toSongWithAudio(): Song? {
        // Extract artist and title from video title
        val titleParts = snippet.title.split("-", limit = 2)
        val artist = if (titleParts.size > 1) titleParts[0].trim() else snippet.channelTitle
        val songTitle = if (titleParts.size > 1) titleParts[1].trim() else snippet.title
        
        // Get best quality thumbnail
        val thumbnail = snippet.thumbnails.maxres?.url 
            ?: snippet.thumbnails.high?.url 
            ?: snippet.thumbnails.medium?.url 
            ?: snippet.thumbnails.default?.url ?: ""
        
        // Try to find actual audio from JioSaavn using the song title
        try {
            val searchQuery = "$artist $songTitle".trim()
            val jioSaavnResponse = jioSaavnApi.searchSongs(searchQuery, limit = 1)
            if (jioSaavnResponse.isSuccessful) {
                val jioSaavnSong = jioSaavnResponse.body()?.data?.results?.firstOrNull()
                if (jioSaavnSong != null) {
                    val audioUrl = jioSaavnSong.downloadUrl?.firstOrNull()?.link ?: jioSaavnSong.url
                    if (audioUrl != null) {
                        return Song(
                            id = "youtube_${id.videoId}",
                            title = songTitle,
                            artist = artist,
                            album = "YouTube Music",
                            albumArtUrl = thumbnail,
                            audioUrl = audioUrl, // Full song from JioSaavn
                            duration = jioSaavnSong.duration?.toLongOrNull()?.times(1000) ?: 180000,
                            genre = "Music",
                            releaseYear = snippet.publishedAt?.take(4)?.toIntOrNull() ?: 2024
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to get audio for ${id.videoId}: ${e.message}")
        }
        
        return null // Skip if no audio found
    }
    
    suspend fun searchSongs(query: String): Flow<List<Song>> = flow {
        if (query.isEmpty()) {
            emit(emptyList())
            return@flow
        }
        
        Log.d("MusicRepository", "Searching for: $query")
        
        try {
            val allResults = mutableListOf<Song>()
            
            // 1. Search in cached songs (instant results - always works)
            val cachedResults = cachedSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true) ||
                it.genre.contains(query, ignoreCase = true)
            }
            allResults.addAll(cachedResults)
            Log.e("MusicRepository", "✓ Cached: ${cachedResults.size} songs")
            
            // 2. Search MusicBrainz for comprehensive music database
            try {
                val mbQuery = "recording:\"$query\" OR artist:\"$query\""
                val mbResponse = musicBrainzApi.searchRecordings(mbQuery, limit = 25)
                if (mbResponse.isSuccessful) {
                    val mbRecordings = mbResponse.body()?.recordings ?: emptyList()
                    val mbSongs = mbRecordings.mapNotNull { it.toSong() }
                    allResults.addAll(mbSongs)
                    Log.e("MusicRepository", "✓ MusicBrainz: ${mbSongs.size} songs")
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "MusicBrainz search error: ${e.message}")
            }

            // 3. Search iTunes for previews (30s) and track metadata
            try {
                val itunesResponse = itunesApi.searchMusic(term = query, limit = 25)
                if (itunesResponse.isSuccessful) {
                    val tracks = itunesResponse.body()?.results ?: emptyList()
                    val itunesSongs = tracks.mapNotNull { it.toSong() }
                    allResults.addAll(itunesSongs)
                    Log.e("MusicRepository", "✓ iTunes: ${itunesSongs.size} preview tracks")
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "iTunes search error: ${e.message}")
            }
            
            val finalResults = allResults.distinctBy { it.id }
            emit(finalResults)
            Log.e("MusicRepository", "✓✓✓ TOTAL SEARCH RESULTS: ${finalResults.size} songs for '$query' ✓✓✓")
        } catch (e: Exception) {
            Log.e("MusicRepository", "Search error: ${e.message}")
            val results = cachedSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
            emit(results)
        }
    }
    
    suspend fun getSongsByGenre(genre: String): Flow<List<Song>> = flow {
        val results = cachedSongs.filter { it.genre.equals(genre, ignoreCase = true) }
        emit(results)
    }
    
    suspend fun toggleFavorite(songId: String) {
        cachedSongs.find { it.id == songId }?.let { song ->
            val index = cachedSongs.indexOf(song)
            cachedSongs[index] = song.copy(isFavorite = !song.isFavorite)
        }
    }
    
    suspend fun getFavoriteSongs(): Flow<List<Song>> = flow {
        emit(cachedSongs.filter { it.isFavorite })
    }
    
    // Search by language/region (e.g., Sinhala, Hindi, Tamil, English)
    suspend fun searchByLanguage(language: String): Flow<List<Song>> = flow {
        try {
            val searchQuery = when(language.lowercase()) {
                "sinhala" -> "sinhala"
                "hindi", "bollywood" -> "bollywood"
                "tamil" -> "tamil"
                "telugu" -> "telugu"
                "english" -> "pop"
                "kpop" -> "korean"
                else -> language
            }
            
            val results = mutableListOf<Song>()
            
            // Search Jamendo for full songs
            try {
                val jamendoResponse = jamendoApi.searchTracks(
                    clientId = ApiClient.JAMENDO_CLIENT_ID,
                    search = searchQuery,
                    limit = 50
                )
                if (jamendoResponse.isSuccessful) {
                    val tracks = jamendoResponse.body()?.results ?: emptyList()
                    results.addAll(tracks.map { it.toSong() })
                    Log.d("MusicRepository", "Jamendo found ${tracks.size} $language songs")
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Jamendo language search error: ${e.message}")
            }
            
            emit(results)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Language search error: ${e.message}")
            emit(emptyList())
        }
    }
    
    // Free full-length music sources
    private fun getFreeMusicArchiveSongs(): List<Song> {
        return listOf(
            Song(
                id = "fma_1",
                title = "Impact Moderato",
                artist = "Kevin MacLeod",
                album = "Impact",
                albumArtUrl = "https://picsum.photos/seed/fma1/500",
                audioUrl = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Kevin_MacLeod/Impact/Kevin_MacLeod_-_Impact_Moderato.mp3",
                duration = 147000,
                genre = "Cinematic",
                releaseYear = 2023
            ),
            Song(
                id = "fma_2",
                title = "Carefree",
                artist = "Kevin MacLeod",
                album = "Royalty Free Collection",
                albumArtUrl = "https://picsum.photos/seed/fma2/500",
                audioUrl = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Kevin_MacLeod/Royalty_Free_Music_Collection/Kevin_MacLeod_-_Carefree.mp3",
                duration = 125000,
                genre = "Happy",
                releaseYear = 2023
            )
        )
    }
    
    private fun getSoundHelixSongs(): List<Song> {
        return (1..16).map { num ->
            Song(
                id = "soundhelix_$num",
                title = "Instrumental Symphony $num",
                artist = "SoundHelix Orchestra",
                album = "Generated Music Vol. ${(num-1)/4 + 1}",
                albumArtUrl = "https://picsum.photos/seed/sh$num/500",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$num.mp3",
                duration = 280000L + (num * 5000L),
                genre = when(num % 4) {
                    0 -> "Classical"
                    1 -> "Electronic"
                    2 -> "Ambient"
                    else -> "Instrumental"
                },
                releaseYear = 2024
            )
        }
    }
    
    private fun getInternetArchiveSongs(): List<Song> {
        return listOf(
            Song(
                id = "ia_1",
                title = "Blue Danube Waltz",
                artist = "Johann Strauss II",
                album = "Classical Masterpieces",
                albumArtUrl = "https://picsum.photos/seed/ia1/500",
                audioUrl = "https://ia802606.us.archive.org/35/items/8CompositionsForClassicalGuitar/01BlueDanubeWaltz.mp3",
                duration = 189000,
                genre = "Classical",
                releaseYear = 1867
            ),
            Song(
                id = "ia_2",
                title = "Moonlight Sonata",
                artist = "Ludwig van Beethoven",
                album = "Piano Sonatas",
                albumArtUrl = "https://picsum.photos/seed/ia2/500",
                audioUrl = "https://ia800905.us.archive.org/16/items/PianoSonataNo.14inmoonlight1stMovement/PianoSonataNo.14inmoonlight-1stMovement.mp3",
                duration = 330000,
                genre = "Classical",
                releaseYear = 1801
            )
        )
    }
    
    private fun getCuratedMusicCollection(): List<Song> {
        return listOf(
            // Curated Music Collection - Professional quality streaming music
            
            // SoundHelix - Professional quality instrumental music
            Song(
                id = "1",
                title = "Midnight Echoes",
                artist = "Luna Wave",
                album = "Digital Dreams",
                albumArtUrl = "https://picsum.photos/seed/song1/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                duration = 295000,
                genre = "Electronic",
                releaseYear = 2024
            ),
            Song(
                id = "2",
                title = "Neon Nights",
                artist = "Cyber Pulse",
                album = "Future Vibes",
                albumArtUrl = "https://picsum.photos/seed/song2/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                duration = 287000,
                genre = "Synthwave",
                releaseYear = 2024
            ),
            Song(
                id = "3",
                title = "Ocean Breeze",
                artist = "Coastal Harmony",
                album = "Tides",
                albumArtUrl = "https://picsum.photos/seed/song3/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                duration = 312000,
                genre = "Ambient",
                releaseYear = 2023
            ),
            Song(
                id = "4",
                title = "Urban Jungle",
                artist = "Street Sounds",
                album = "City Life",
                albumArtUrl = "https://picsum.photos/seed/song4/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                duration = 278000,
                genre = "Hip Hop",
                releaseYear = 2024
            ),
            Song(
                id = "5",
                title = "Starlight Serenade",
                artist = "Celestial Choir",
                album = "Cosmos",
                albumArtUrl = "https://picsum.photos/seed/song5/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                duration = 342000,
                genre = "Classical",
                releaseYear = 2023
            ),
            Song(
                id = "6",
                title = "Thunder Road",
                artist = "Rock Legends",
                album = "Highway Tales",
                albumArtUrl = "https://picsum.photos/seed/song6/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                duration = 256000,
                genre = "Rock",
                releaseYear = 2024
            ),
            Song(
                id = "7",
                title = "Tropical Paradise",
                artist = "Island Groove",
                album = "Summer Vibes",
                albumArtUrl = "https://picsum.photos/seed/song7/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                duration = 289000,
                genre = "Reggae",
                releaseYear = 2024
            ),
            Song(
                id = "8",
                title = "Digital Love",
                artist = "Tech Hearts",
                album = "Algorithmic Emotions",
                albumArtUrl = "https://picsum.photos/seed/song8/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                duration = 301000,
                genre = "Pop",
                releaseYear = 2024
            ),
            Song(
                id = "9",
                title = "Symphony No. 9",
                artist = "Global Orchestra",
                album = "Classical Masterpieces",
                albumArtUrl = "https://picsum.photos/seed/song9/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                duration = 324000,
                genre = "Classical",
                releaseYear = 2023
            ),
            Song(
                id = "10",
                title = "Mountain High",
                artist = "Nature Beats",
                album = "Earth Elements",
                albumArtUrl = "https://picsum.photos/seed/song10/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                duration = 298000,
                genre = "Ambient",
                releaseYear = 2024
            ),
            Song(
                id = "11",
                title = "Jazz Cafe",
                artist = "Smooth Groove",
                album = "Late Night Sessions",
                albumArtUrl = "https://picsum.photos/seed/song11/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                duration = 276000,
                genre = "Jazz",
                releaseYear = 2024
            ),
            Song(
                id = "12",
                title = "Electric Storm",
                artist = "Voltage",
                album = "Power Grid",
                albumArtUrl = "https://picsum.photos/seed/song12/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                duration = 267000,
                genre = "Electronic",
                releaseYear = 2024
            ),
            Song(
                id = "13",
                title = "Desert Wind",
                artist = "Sahara Sounds",
                album = "Nomad Journey",
                albumArtUrl = "https://picsum.photos/seed/song13/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
                duration = 315000,
                genre = "World",
                releaseYear = 2023
            ),
            Song(
                id = "14",
                title = "Cyber Dreams",
                artist = "Future Shock",
                album = "2099",
                albumArtUrl = "https://picsum.photos/seed/song14/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
                duration = 289000,
                genre = "Synthwave",
                releaseYear = 2024
            ),
            Song(
                id = "15",
                title = "Moonlight Dance",
                artist = "Luna Collective",
                album = "Night Moves",
                albumArtUrl = "https://picsum.photos/seed/song15/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
                duration = 294000,
                genre = "Dance",
                releaseYear = 2024
            ),
            Song(
                id = "16",
                title = "Acoustic Sunrise",
                artist = "Morning Light",
                album = "Dawn Chorus",
                albumArtUrl = "https://picsum.photos/seed/song16/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                duration = 281000,
                genre = "Folk",
                releaseYear = 2024
            ),
            
            // Free Music Archive - Public domain and Creative Commons music
            Song(
                id = "17",
                title = "Epic Journey",
                artist = "Adventure Sounds",
                album = "Quest",
                albumArtUrl = "https://picsum.photos/seed/song17/300",
                audioUrl = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Kevin_MacLeod/Impact/Kevin_MacLeod_-_Impact_Moderato.mp3",
                duration = 147000,
                genre = "Cinematic",
                releaseYear = 2023
            ),
            Song(
                id = "18",
                title = "Carefree",
                artist = "Kevin MacLeod",
                album = "Upbeat Collection",
                albumArtUrl = "https://picsum.photos/seed/song18/300",
                audioUrl = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Kevin_MacLeod/Royalty_Free_Music_Collection/Kevin_MacLeod_-_Carefree.mp3",
                duration = 125000,
                genre = "Happy",
                releaseYear = 2023
            ),
            Song(
                id = "19",
                title = "Space Ambient",
                artist = "Galaxy Explorer",
                album = "Cosmic Sounds",
                albumArtUrl = "https://picsum.photos/seed/song19/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                duration = 305000,
                genre = "Ambient",
                releaseYear = 2024
            ),
            Song(
                id = "20",
                title = "Funky Groove",
                artist = "Bass Masters",
                album = "Groove Street",
                albumArtUrl = "https://picsum.photos/seed/song20/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                duration = 268000,
                genre = "Funk",
                releaseYear = 2024
            ),
            Song(
                id = "21",
                title = "Chill Vibes",
                artist = "Lo-Fi Beats",
                album = "Study Session",
                albumArtUrl = "https://picsum.photos/seed/song21/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                duration = 293000,
                genre = "Lo-Fi",
                releaseYear = 2024
            ),
            Song(
                id = "22",
                title = "Piano Reflections",
                artist = "Ivory Dreams",
                album = "Solo Piano",
                albumArtUrl = "https://picsum.photos/seed/song22/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                duration = 287000,
                genre = "Classical",
                releaseYear = 2023
            ),
            Song(
                id = "23",
                title = "Reggae Sunshine",
                artist = "Caribbean Crew",
                album = "Island Life",
                albumArtUrl = "https://picsum.photos/seed/song23/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                duration = 274000,
                genre = "Reggae",
                releaseYear = 2024
            ),
            Song(
                id = "24",
                title = "Metal Storm",
                artist = "Iron Thunder",
                album = "Unleashed",
                albumArtUrl = "https://picsum.photos/seed/song24/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                duration = 249000,
                genre = "Metal",
                releaseYear = 2024
            ),
            Song(
                id = "25",
                title = "Country Road",
                artist = "Nashville Stars",
                album = "Southern Stories",
                albumArtUrl = "https://picsum.photos/seed/song25/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                duration = 284000,
                genre = "Country",
                releaseYear = 2024
            ),
            Song(
                id = "26",
                title = "EDM Blast",
                artist = "Festival Beats",
                album = "Main Stage",
                albumArtUrl = "https://picsum.photos/seed/song26/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                duration = 245000,
                genre = "EDM",
                releaseYear = 2024
            ),
            Song(
                id = "27",
                title = "Meditation Flow",
                artist = "Zen Masters",
                album = "Inner Peace",
                albumArtUrl = "https://picsum.photos/seed/song27/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                duration = 356000,
                genre = "Meditation",
                releaseYear = 2023
            ),
            Song(
                id = "28",
                title = "Hip Hop Beat",
                artist = "Urban Poets",
                album = "Street Philosophy",
                albumArtUrl = "https://picsum.photos/seed/song28/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                duration = 271000,
                genre = "Hip Hop",
                releaseYear = 2024
            ),
            Song(
                id = "29",
                title = "Salsa Caliente",
                artist = "Latin Fire",
                album = "Baila Conmigo",
                albumArtUrl = "https://picsum.photos/seed/song29/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                duration = 263000,
                genre = "Latin",
                releaseYear = 2024
            ),
            Song(
                id = "30",
                title = "Blues Night",
                artist = "Delta Blues Band",
                album = "Midnight Train",
                albumArtUrl = "https://picsum.photos/seed/song30/300",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                duration = 298000,
                genre = "Blues",
                releaseYear = 2023
            )
        )
    }
    
    // YouTube Piped conversion functions
    private fun com.modernmusicplayer.app.data.api.PipedVideo.toSong(): Song {
        // Extract video ID from URL (e.g., "/watch?v=VIDEO_ID")
        val videoId = this.url.substringAfter("v=").substringBefore("&")
        
        // Extract artist and title from video title
        val titleParts = this.title.split("-", limit = 2)
        val artist = if (titleParts.size > 1) titleParts[0].trim() else (this.uploaderName ?: "Unknown")
        val title = if (titleParts.size > 1) titleParts[1].trim() else this.title
        
        return Song(
            id = "yt_$videoId",
            title = title,
            artist = artist,
            album = "YouTube",
            albumArtUrl = this.thumbnail,
            audioUrl = "piped://$videoId", // Special URL scheme for Piped extraction
            duration = this.duration * 1000, // Convert seconds to milliseconds
            genre = "Music",
            releaseYear = 0
        )
    }
    
    // Favorites management
    fun toggleFavoritePersistent(songId: String): Boolean {
        return prefsManager.toggleFavorite(songId)
    }
    
    fun isFavoritePersistent(songId: String): Boolean {
        return prefsManager.isFavorite(songId)
    }
    
    suspend fun getFavoriteSongsPersistent(): Flow<List<Song>> = flow {
        val favoriteIds = prefsManager.getFavorites()
        val allSongs = cachedSongs + localMusicCache
        val favoriteSongs = allSongs.filter { favoriteIds.contains(it.id) }
        emit(favoriteSongs)
    }
    
    // Recently played management
    fun addRecentlyPlayed(songId: String) {
        prefsManager.addRecentTrack(songId)
    }
    
    suspend fun getRecentlyPlayed(): Flow<List<Song>> = flow {
        val recentIds = prefsManager.getRecentTrackIds()
        val allSongs = cachedSongs + localMusicCache
        val recentSongs = recentIds.mapNotNull { id ->
            allSongs.find { it.id == id }
        }
        emit(recentSongs)
    }
    
    // Local music cache management
    fun cacheLocalMusic(songs: List<Song>) {
        localMusicCache.clear()
        localMusicCache.addAll(songs)
        Log.d("MusicRepository", "Cached ${songs.size} local songs")
    }
    
    fun getLocalMusicCache(): List<Song> {
        return localMusicCache.toList()
    }
    
    suspend fun getAllAvailableSongs(): Flow<List<Song>> = flow {
        val allSongs = (cachedSongs + localMusicCache).distinctBy { it.id }
        emit(allSongs)
    }
    
}
