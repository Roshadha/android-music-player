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

// Free Music API - 320kbps songs from multiple engines
interface FreeMusicApiService {
    
    @GET("search")
    suspend fun searchSongs(
        @Query("q") query: String,
        @Query("searchEngine") searchEngine: String = "gaama"
    ): Response<FreeMusicSearchResponse>
    
    @GET("fetch")
    suspend fun fetchSong(
        @Query("id") id: String
    ): Response<FreeMusicFetchResponse>
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

// Free Music API Response Models
data class FreeMusicSearchResponse(
    val status: Int,
    val response: List<FreeMusicTrack>,
    val message: String
)

data class FreeMusicTrack(
    val id: String,
    val title: String,
    val album: String?,
    val artist: String?,
    val img: String?
)

data class FreeMusicFetchResponse(
    val status: Int,
    val response: String, // Audio URL
    val message: String
)

// MusicBrainz API Service - Comprehensive music metadata
interface MusicBrainzApiService {
    
    @GET("recording")
    suspend fun searchRecordings(
        @Query("query") query: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("fmt") format: String = "json"
    ): Response<MusicBrainzRecordingResponse>
    
    @GET("artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("limit") limit: Int = 25,
        @Query("fmt") format: String = "json"
    ): Response<MusicBrainzArtistResponse>
    
    @GET("release")
    suspend fun searchReleases(
        @Query("query") query: String,
        @Query("limit") limit: Int = 25,
        @Query("fmt") format: String = "json"
    ): Response<MusicBrainzReleaseResponse>
    
    @GET("recording/{mbid}")
    suspend fun getRecordingDetails(
        @Path("mbid") mbid: String,
        @Query("inc") include: String = "artists+releases+url-rels",
        @Query("fmt") format: String = "json"
    ): Response<MusicBrainzRecording>
}

// MusicBrainz Response Models
data class MusicBrainzRecordingResponse(
    val recordings: List<MusicBrainzRecording>,
    val count: Int,
    val offset: Int
)

data class MusicBrainzRecording(
    val id: String, // MBID
    val title: String,
    val length: Int? = null, // Duration in milliseconds
    val score: Int? = null,
    val video: Boolean? = null,
    val disambiguation: String? = null,
    val aliases: List<MusicBrainzAlias>? = null,
    val tags: List<MusicBrainzTag>? = null,
    val genres: List<MusicBrainzTag>? = null,
    val rating: MusicBrainzRating? = null,
    val releases: List<MusicBrainzRelease>? = null,
    val relations: List<MusicBrainzRelation>? = null
) {
    // Artist credits field with custom name
    @com.google.gson.annotations.SerializedName("artist-credit")
    val artistCredit: List<MusicBrainzArtistCredit>? = null
}

data class MusicBrainzArtistCredit(
    val name: String,
    val artist: MusicBrainzArtist
)

data class MusicBrainzArtist(
    val id: String,
    val name: String,
    val disambiguation: String? = null,
    val aliases: List<MusicBrainzAlias>? = null
) {
    @com.google.gson.annotations.SerializedName("sort-name")
    val sortName: String? = null
}

data class MusicBrainzRelease(
    val id: String,
    val title: String,
    val status: String? = null,
    val date: String? = null,
    val country: String? = null,
    val media: List<MusicBrainzMedia>? = null
) {
    @com.google.gson.annotations.SerializedName("release-group")
    val releaseGroup: MusicBrainzReleaseGroup? = null
}

data class MusicBrainzReleaseGroup(
    val id: String,
    val title: String,
    @com.google.gson.annotations.SerializedName("primary-type")
    val primaryType: String? = null,
    @com.google.gson.annotations.SerializedName("secondary-types")
    val secondaryTypes: List<String>? = null
)

data class MusicBrainzMedia(
    val format: String? = null,
    @com.google.gson.annotations.SerializedName("track-count")
    val trackCount: Int? = null
)

data class MusicBrainzAlias(
    val name: String,
    val locale: String? = null,
    val type: String? = null,
    val primary: Boolean? = null
)

data class MusicBrainzTag(
    val name: String,
    val count: Int
)

data class MusicBrainzRating(
    val value: Double?,
    @com.google.gson.annotations.SerializedName("votes-count")
    val votesCount: Int
)

data class MusicBrainzRelation(
    val type: String,
    @com.google.gson.annotations.SerializedName("type-id")
    val typeId: String,
    val direction: String,
    val url: MusicBrainzUrl? = null,
    val artist: MusicBrainzArtist? = null
)

data class MusicBrainzUrl(
    val id: String,
    val resource: String
)

data class MusicBrainzArtistResponse(
    val artists: List<MusicBrainzArtist>,
    val count: Int
)

data class MusicBrainzReleaseResponse(
    val releases: List<MusicBrainzRelease>,
    val count: Int
)


