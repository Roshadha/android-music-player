package com.modernmusicplayer.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Invidious API - Free YouTube frontend API
 * No API key required, works without limits
 */
interface InvidiousApiService {
    
    /**
     * Search for videos
     */
    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("page") page: Int = 1
    ): List<InvidiousVideo>
    
    /**
     * Get trending videos
     */
    @GET("api/v1/trending")
    suspend fun getTrending(
        @Query("type") type: String = "music"
    ): List<InvidiousVideo>
    
    /**
     * Get video details including audio formats
     */
    @GET("api/v1/videos/{videoId}")
    suspend fun getVideo(
        @Path("videoId") videoId: String
    ): InvidiousVideoDetail
}

/**
 * Video search result
 */
data class InvidiousVideo(
    val videoId: String,
    val title: String,
    val author: String,
    val lengthSeconds: Int,
    val videoThumbnails: List<Thumbnail>
)

/**
 * Video detail with audio formats
 */
data class InvidiousVideoDetail(
    val videoId: String,
    val title: String,
    val author: String,
    val lengthSeconds: Int,
    val videoThumbnails: List<Thumbnail>,
    val adaptiveFormats: List<AudioFormat>
)

data class Thumbnail(
    val url: String,
    val quality: String,
    val width: Int,
    val height: Int
)

data class AudioFormat(
    val url: String,
    val type: String,
    val bitrate: Int,
    val audioQuality: String?
)
