package com.modernmusicplayer.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.Response

interface MusicApiService {
    
    // Deezer API - Global music search (for metadata only)
    @GET("search")
    suspend fun searchDeezer(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50
    ): Response<DeezerSearchResponse>
    
    @GET("chart")
    suspend fun getDeezerChart(
        @Query("limit") limit: Int = 100
    ): Response<DeezerChartResponse>
}

// iTunes API Service - 30 second previews
interface ITunesApiService {
    
    @GET("search")
    suspend fun searchMusic(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): Response<ITunesSearchResponse>
    
    @GET("lookup")
    suspend fun lookupSong(
        @Query("id") id: String,
        @Query("entity") entity: String = "song"
    ): Response<ITunesSearchResponse>
}

// JioSaavn API Service for full songs
interface JioSaavnApiService {
    
    @GET("search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<JioSaavnSearchResponse>
    
    @GET("modules")
    suspend fun getTrendingSongs(
        @Query("language") language: String = "english"
    ): Response<JioSaavnTrendingResponse>
}

// Jamendo API Service - Full free music
interface JamendoApiService {
    
    @GET("tracks")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 50,
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): Response<JamendoTracksResponse>
    
    @GET("tracks")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("search") search: String,
        @Query("limit") limit: Int = 50,
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): Response<JamendoTracksResponse>
}

// Deezer API Response Models
data class DeezerSearchResponse(
    val data: List<DeezerTrack>,
    val total: Int
)

data class DeezerChartResponse(
    val tracks: DeezerTracksData
)

data class DeezerTracksData(
    val data: List<DeezerTrack>
)

data class DeezerTrack(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String, // 30-second preview URL
    val artist: DeezerArtist,
    val album: DeezerAlbum
)

data class DeezerArtist(
    val id: Long,
    val name: String,
    val picture: String? = null,
    val picture_medium: String? = null
)

data class DeezerAlbum(
    val id: Long,
    val title: String,
    val cover: String? = null,
    val cover_medium: String? = null,
    val cover_big: String? = null
)

// JioSaavn API Response Models
data class JioSaavnSearchResponse(
    val success: Boolean,
    val data: JioSaavnSearchData?
)

data class JioSaavnSearchData(
    val total: Int,
    val results: List<JioSaavnSong>
)

data class JioSaavnTrendingResponse(
    val status: String,
    val data: JioSaavnTrendingData?
)

data class JioSaavnTrendingData(
    val albums: List<JioSaavnSong>? = null,
    val trending: JioSaavnTrendingSection? = null
)

data class JioSaavnTrendingSection(
    val songs: List<JioSaavnSong>? = null
)

data class JioSaavnSong(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val artists: String? = null,
    val image: String? = null,
    val url: String? = null,
    val downloadUrl: List<JioSaavnDownloadUrl>? = null,
    val duration: String? = null,
    val year: String? = null,
    val language: String? = null,
    val album: String? = null
)

data class JioSaavnDownloadUrl(
    val quality: String,
    val link: String
)

// Jamendo API Response Models
data class JamendoTracksResponse(
    val results: List<JamendoTrack>
)

data class JamendoTrack(
    val id: String,
    val name: String,
    val artist_name: String,
    val album_name: String,
    val album_image: String,
    val audio: String,
    val audiodownload: String,
    val duration: Int,
    val releasedate: String? = null
)

// YouTube Data API v3 Service
interface YouTubeApiService {
    
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") categoryId: String = "10", // Music category
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
    
    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,snippet",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoDetailsResponse>
}

// YouTube API Response Models
data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>?,
    val pageInfo: YouTubePageInfo?
)

data class YouTubeSearchItem(
    val id: YouTubeVideoId,
    val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    val videoId: String
)

data class YouTubeSnippet(
    val title: String,
    val description: String?,
    val channelTitle: String,
    val thumbnails: YouTubeThumbnails,
    val publishedAt: String?
)

data class YouTubeThumbnails(
    val default: YouTubeThumbnail?,
    val medium: YouTubeThumbnail?,
    val high: YouTubeThumbnail?,
    val maxres: YouTubeThumbnail?
)

data class YouTubeThumbnail(
    val url: String,
    val width: Int?,
    val height: Int?
)

data class YouTubePageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class YouTubeVideoDetailsResponse(
    val items: List<YouTubeVideoDetail>?
)

data class YouTubeVideoDetail(
    val id: String,
    val snippet: YouTubeSnippet,
    val contentDetails: YouTubeContentDetails
)

data class YouTubeContentDetails(
    val duration: String // ISO 8601 format (e.g., PT4M13S)
)

// iTunes API Response Models
data class ITunesSearchResponse(
    val resultCount: Int,
    val results: List<ITunesTrack>
)

data class ITunesTrack(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String?, // Album name
    val artworkUrl100: String?,
    val artworkUrl60: String?,
    val previewUrl: String?, // 30-second preview URL
    val trackTimeMillis: Long?,
    val primaryGenreName: String?,
    val releaseDate: String?
)

