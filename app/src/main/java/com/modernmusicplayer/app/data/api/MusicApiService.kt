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

// TheAudioDB API Service - Music videos and trending
interface TheAudioDbApiService {
    
    @GET("search.php")
    suspend fun searchArtist(
        @Query("s") artistName: String
    ): Response<AudioDbArtistResponse>
    
    @GET("searchalbum.php")
    suspend fun searchAlbums(
        @Query("s") artistName: String
    ): Response<AudioDbAlbumResponse>
    
    @GET("searchalbum.php")
    suspend fun searchAlbumByName(
        @Query("s") artistName: String,
        @Query("a") albumName: String
    ): Response<AudioDbAlbumResponse>
    
    @GET("searchtrack.php")
    suspend fun searchTrack(
        @Query("s") artistName: String,
        @Query("t") trackName: String
    ): Response<AudioDbTrackResponse>
    
    @GET("track.php")
    suspend fun getTracksByAlbum(
        @Query("m") albumId: String
    ): Response<AudioDbTrackResponse>
    
    @GET("mvid.php")
    suspend fun getMusicVideos(
        @Query("i") artistId: String
    ): Response<AudioDbMusicVideoResponse>
    
    @GET("track-top10.php")
    suspend fun getTop10Tracks(
        @Query("s") artistName: String
    ): Response<AudioDbTrackResponse>
    
    @GET("trending.php")
    suspend fun getTrendingSongs(
        @Query("country") country: String = "us",
        @Query("type") type: String = "itunes",
        @Query("format") format: String = "singles"
    ): Response<AudioDbTrendingResponse>
}

// TheAudioDB Response Models
data class AudioDbArtistResponse(
    val artists: List<AudioDbArtist>?
)

data class AudioDbArtist(
    val idArtist: String,
    val strArtist: String,
    val strArtistAlternate: String? = null,
    val strLabel: String? = null,
    val idLabel: String? = null,
    val intFormedYear: String? = null,
    val intBornYear: String? = null,
    val intDiedYear: String? = null,
    val strDisbanded: String? = null,
    val strStyle: String? = null,
    val strGenre: String? = null,
    val strMood: String? = null,
    val strWebsite: String? = null,
    val strFacebook: String? = null,
    val strTwitter: String? = null,
    val strBiographyEN: String? = null,
    val strBiographyCN: String? = null,
    val strBiographyDE: String? = null,
    val strBiographyFR: String? = null,
    val strBiographyIT: String? = null,
    val strBiographyJP: String? = null,
    val strBiographyRU: String? = null,
    val strBiographyES: String? = null,
    val strBiographyPT: String? = null,
    val strBiographySE: String? = null,
    val strBiographyNL: String? = null,
    val strBiographyHU: String? = null,
    val strBiographyNO: String? = null,
    val strBiographyIL: String? = null,
    val strBiographyPL: String? = null,
    val strGender: String? = null,
    val intMembers: String? = null,
    val strCountry: String? = null,
    val strCountryCode: String? = null,
    val strArtistThumb: String? = null,
    val strArtistLogo: String? = null,
    val strArtistCutout: String? = null,
    val strArtistClearart: String? = null,
    val strArtistWideThumb: String? = null,
    val strArtistFanart: String? = null,
    val strArtistFanart2: String? = null,
    val strArtistFanart3: String? = null,
    val strArtistFanart4: String? = null,
    val strArtistBanner: String? = null,
    val strMusicBrainzID: String? = null,
    val strLastFMChart: String? = null,
    val intCharted: String? = null,
    val strLocked: String? = null
)

data class AudioDbAlbumResponse(
    val album: List<AudioDbAlbum>?
)

data class AudioDbAlbum(
    val idAlbum: String,
    val idArtist: String,
    val strAlbum: String,
    val strArtist: String,
    val intYearReleased: String? = null,
    val strStyle: String? = null,
    val strGenre: String? = null,
    val strLabel: String? = null,
    val strReleaseFormat: String? = null,
    val intSales: String? = null,
    val strAlbumThumb: String? = null,
    val strAlbumThumbHQ: String? = null,
    val strAlbumCDart: String? = null,
    val strAlbumSpine: String? = null,
    val strAlbum3DCase: String? = null,
    val strAlbum3DFlat: String? = null,
    val strAlbum3DFace: String? = null,
    val strAlbum3DThumb: String? = null,
    val strDescriptionEN: String? = null,
    val intLoved: String? = null,
    val intScore: String? = null,
    val intScoreVotes: String? = null,
    val strReview: String? = null,
    val strMood: String? = null,
    val strTheme: String? = null,
    val strSpeed: String? = null,
    val strLocation: String? = null,
    val strMusicBrainzID: String? = null,
    val strMusicBrainzArtistID: String? = null,
    val strAllMusicID: String? = null,
    val strBBCReviewID: String? = null,
    val strRateYourMusicID: String? = null,
    val strDiscogsID: String? = null,
    val strWikidataID: String? = null,
    val strWikipediaID: String? = null,
    val strGeniusID: String? = null,
    val strLyricWikiID: String? = null,
    val strMusicMozID: String? = null,
    val strItunesID: String? = null,
    val strAmazonID: String? = null,
    val strLocked: String? = null
)

data class AudioDbTrackResponse(
    val track: List<AudioDbTrack>?
)

data class AudioDbTrack(
    val idTrack: String,
    val idAlbum: String? = null,
    val idArtist: String? = null,
    val idLyric: String? = null,
    val idIMVDB: String? = null,
    val strTrack: String,
    val strAlbum: String? = null,
    val strArtist: String? = null,
    val strArtistAlternate: String? = null,
    val intCD: String? = null,
    val intDuration: String? = null, // Duration in milliseconds as string
    val strGenre: String? = null,
    val strMood: String? = null,
    val strStyle: String? = null,
    val strTheme: String? = null,
    val strDescriptionEN: String? = null,
    val strTrackThumb: String? = null,
    val strTrack3x3: String? = null,
    val strTrackLyrics: String? = null,
    val strMusicVid: String? = null, // YouTube URL!
    val strMusicVidDirector: String? = null,
    val strMusicVidCompany: String? = null,
    val strMusicVidScreen1: String? = null,
    val strMusicVidScreen2: String? = null,
    val strMusicVidScreen3: String? = null,
    val intMusicVidViews: String? = null,
    val intMusicVidLikes: String? = null,
    val intMusicVidDislikes: String? = null,
    val intMusicVidFavorites: String? = null,
    val intMusicVidComments: String? = null,
    val intTrackNumber: String? = null,
    val intLoved: String? = null,
    val intScore: String? = null,
    val intScoreVotes: String? = null,
    val intTotalListeners: String? = null,
    val intTotalPlays: String? = null,
    val strMusicBrainzID: String? = null,
    val strMusicBrainzAlbumID: String? = null,
    val strMusicBrainzArtistID: String? = null,
    val strLocked: String? = null
)

data class AudioDbMusicVideoResponse(
    val mvids: List<AudioDbMusicVideo>?
)

data class AudioDbMusicVideo(
    val idArtist: String,
    val idAlbum: String? = null,
    val idTrack: String? = null,
    val strTrack: String,
    val strTrackThumb: String? = null,
    val strMusicVid: String? = null, // YouTube URL
    val strDescriptionEN: String? = null
)

data class AudioDbTrendingResponse(
    val trending: List<AudioDbTrendingTrack>?
)

data class AudioDbTrendingTrack(
    val idTrend: String,
    val intChartPlace: String? = null,
    val strTrack: String,
    val strArtist: String,
    val strAlbum: String? = null,
    val strTrackThumb: String? = null,
    val strCountry: String? = null,
    val strType: String? = null,
    val strFormat: String? = null,
    val dateAdded: String? = null
)


