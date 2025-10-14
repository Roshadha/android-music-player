# API Integration Guide

## Spotify API Integration

### Prerequisites
1. Spotify Developer Account
2. Register your app at https://developer.spotify.com/dashboard

### Setup Steps

#### 1. Get Credentials
```kotlin
// Add to local.properties (don't commit this file)
spotify.client.id=YOUR_CLIENT_ID
spotify.client.secret=YOUR_CLIENT_SECRET
```

#### 2. Add Dependencies
```gradle
// In app/build.gradle
dependencies {
    implementation 'com.spotify.android:auth:1.2.5'
    implementation 'com.spotify.sdk:spotify-player-24-noconnect-2.20b@aar'
}
```

#### 3. Create Spotify Service
```kotlin
interface SpotifyApiService {
    @GET("v1/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 50,
        @Header("Authorization") auth: String
    ): SpotifySearchResponse
    
    @GET("v1/tracks/{id}")
    suspend fun getTrack(
        @Path("id") trackId: String,
        @Header("Authorization") auth: String
    ): SpotifyTrack
}
```

#### 4. Update MusicRepository
```kotlin
class MusicRepository(
    private val spotifyService: SpotifyApiService,
    private val accessToken: String
) {
    suspend fun getGlobalSongs(limit: Int = 50): Result<List<Song>> {
        return try {
            val response = spotifyService.searchTracks(
                query = "year:2024",
                limit = limit,
                auth = "Bearer $accessToken"
            )
            
            val songs = response.tracks.items.map { track ->
                Song(
                    id = track.id,
                    title = track.name,
                    artist = track.artists.first().name,
                    album = track.album.name,
                    albumArtUrl = track.album.images.first().url,
                    audioUrl = track.preview_url ?: "",
                    duration = track.duration_ms.toLong()
                )
            }
            
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Deezer API Integration

### Setup (No Authentication Required for Basic Use)

#### 1. Add Retrofit Service
```kotlin
interface DeezerApiService {
    @GET("chart/0/tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 50
    ): DeezerTracksResponse
    
    @GET("search/track")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50
    ): DeezerSearchResponse
}
```

#### 2. Create Retrofit Instance
```kotlin
object DeezerApi {
    private const val BASE_URL = "https://api.deezer.com/"
    
    val service: DeezerApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerApiService::class.java)
    }
}
```

#### 3. Use in Repository
```kotlin
suspend fun getGlobalSongs(): Result<List<Song>> {
    return try {
        val response = DeezerApi.service.getTopTracks()
        val songs = response.data.map { track ->
            Song(
                id = track.id.toString(),
                title = track.title,
                artist = track.artist.name,
                albumArtUrl = track.album.cover_big,
                audioUrl = track.preview,
                duration = track.duration * 1000L
            )
        }
        Result.success(songs)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Custom Backend Integration

### 1. Design Your API

#### Endpoints:
```
GET  /api/songs              - Get all songs
GET  /api/songs/{id}         - Get specific song
GET  /api/songs/search?q=    - Search songs
GET  /api/playlists          - Get playlists
POST /api/playlists          - Create playlist
GET  /api/songs/trending     - Get trending
```

### 2. Create Data Models
```kotlin
data class ApiSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val audioUrl: String,
    val durationMs: Long,
    val genre: String
)

data class SongsResponse(
    val songs: List<ApiSong>,
    val total: Int,
    val page: Int
)
```

### 3. Implement Retrofit Service
```kotlin
interface MusicApiService {
    @GET("api/songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): SongsResponse
    
    @GET("api/songs/{id}")
    suspend fun getSongById(
        @Path("id") songId: String
    ): ApiSong
    
    @GET("api/songs/search")
    suspend fun searchSongs(
        @Query("q") query: String
    ): SongsResponse
}
```

### 4. Create Retrofit Instance
```kotlin
object ApiClient {
    private const val BASE_URL = "https://your-backend.com/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val service: MusicApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApiService::class.java)
    }
}
```

## Audio Streaming Formats

### Supported Formats:
- MP3 (most common)
- M4A / AAC
- OGG Vorbis
- FLAC (lossless)
- WAV (uncompressed)

### Recommended Format:
**MP3 at 128-320 kbps** for best compatibility and size

### ExoPlayer Configuration
```kotlin
val player = ExoPlayer.Builder(context)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build(),
        true
    )
    .setHandleAudioBecomingNoisy(true)
    .setWakeMode(C.WAKE_MODE_LOCAL)
    .build()
```

## Caching Strategy

### Enable HTTP Caching
```kotlin
val cacheSize = 100 * 1024 * 1024 // 100 MB
val cache = Cache(context.cacheDir, cacheSize)

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()
```

### ExoPlayer Cache
```kotlin
val databaseProvider = StandaloneDatabaseProvider(context)
val cache = SimpleCache(
    File(context.cacheDir, "media"),
    LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024), // 100 MB
    databaseProvider
)

val dataSourceFactory = CacheDataSource.Factory()
    .setCache(cache)
    .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
```

## Authentication Headers

### Bearer Token
```kotlin
class AuthInterceptor(private val tokenProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider()}")
            .build()
        return chain.proceed(request)
    }
}
```

### API Key
```kotlin
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        
        val request = chain.request().newBuilder()
            .url(url)
            .build()
            
        return chain.proceed(request)
    }
}
```

## Error Handling

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        ApiResult.Error(e.message(), e.code())
    } catch (e: IOException) {
        ApiResult.Error("Network error: ${e.message}")
    } catch (e: Exception) {
        ApiResult.Error("Unknown error: ${e.message}")
    }
}
```

## Rate Limiting

```kotlin
class RateLimitInterceptor(
    private val maxRequests: Int = 100,
    private val timeWindow: Long = 60_000 // 1 minute
) : Interceptor {
    private val requestTimes = mutableListOf<Long>()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(requestTimes) {
            val now = System.currentTimeMillis()
            requestTimes.removeAll { it < now - timeWindow }
            
            if (requestTimes.size >= maxRequests) {
                throw IOException("Rate limit exceeded")
            }
            
            requestTimes.add(now)
        }
        
        return chain.proceed(chain.request())
    }
}
```

## Testing Your Integration

```kotlin
@Test
fun `test fetch songs from API`() = runBlocking {
    val repository = MusicRepository()
    val result = repository.getGlobalSongs()
    
    assertTrue(result.isSuccess)
    val songs = result.getOrNull()
    assertNotNull(songs)
    assertTrue(songs!!.isNotEmpty())
}
```

---

Choose the API that best fits your needs and budget. Spotify offers the richest catalog but requires OAuth. Deezer is simpler but has limited features. A custom backend gives you full control but requires maintenance.
