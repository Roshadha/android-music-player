package com.modernmusicplayer.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Piped API - Alternative YouTube frontend (more reliable than Invidious)
 * Public instance: https://pipedapi.kavin.rocks
 */
interface PipedApiService {
    
    /**
     * Search for videos
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_songs"
    ): PipedSearchResponse
    
    /**
     * Get trending videos
     */
    @GET("trending")
    suspend fun getTrending(
        @Query("region") region: String = "US"
    ): List<PipedVideo>
    
    /**
     * Get video streams (audio URLs)
     */
    @GET("streams/{videoId}")
    suspend fun getStreams(
        @Path("videoId") videoId: String
    ): PipedStreamResponse
}

/**
 * Search response
 */
data class PipedSearchResponse(
    val items: List<PipedVideo>,
    val nextpage: String?,
    val suggestion: String?,
    val corrected: Boolean
)

/**
 * Video item
 */
data class PipedVideo(
    val url: String,  // e.g., "/watch?v=VIDEO_ID"
    val title: String,
    val thumbnail: String,
    val uploaderName: String?,
    val uploaderUrl: String?,
    val uploadedDate: String?,
    val shortDescription: String?,
    val duration: Long,  // in seconds
    val views: Long,
    val uploaded: Long,  // timestamp
    val uploaderVerified: Boolean?
)

/**
 * Stream response with audio URLs
 */
data class PipedStreamResponse(
    val title: String,
    val description: String?,
    val uploadDate: String?,
    val uploader: String?,
    val uploaderUrl: String?,
    val uploaderAvatar: String?,
    val thumbnailUrl: String?,
    val duration: Long,
    val views: Long,
    val likes: Long?,
    val dislikes: Long?,
    val audioStreams: List<PipedAudioStream>,
    val videoStreams: List<PipedVideoStream>?,
    val relatedStreams: List<PipedVideo>?
)

data class PipedAudioStream(
    val url: String,
    val format: String,  // e.g., "M4A", "WEBMA_OPUS"
    val quality: String,  // e.g., "128k", "160k"
    val mimeType: String,
    val codec: String?,
    val audioTrackId: String?,
    val audioTrackName: String?,
    val audioTrackType: String?,
    val audioTrackLocale: String?,
    val videoOnly: Boolean,
    val bitrate: Int,
    val initStart: Int,
    val initEnd: Int,
    val indexStart: Int,
    val indexEnd: Int,
    val width: Int?,
    val height: Int?,
    val fps: Int?,
    val contentLength: Long
)

data class PipedVideoStream(
    val url: String,
    val format: String,
    val quality: String,
    val mimeType: String,
    val codec: String?,
    val videoOnly: Boolean,
    val bitrate: Int,
    val initStart: Int,
    val initEnd: Int,
    val indexStart: Int,
    val indexEnd: Int,
    val width: Int,
    val height: Int,
    val fps: Int,
    val contentLength: Long
)
