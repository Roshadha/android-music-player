package com.modernmusicplayer.app.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLDecoder

/**
 * YouTube audio extractor
 * Extracts direct audio stream URLs from YouTube videos
 */
object YouTubeExtractor {
    
    private const val TAG = "YouTubeExtractor"
    
    /**
     * Extract audio URL from YouTube video ID
     * Returns the best quality audio stream URL
     */
    suspend fun getAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            // Get video info from YouTube
            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            val connection = URL(videoUrl).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            val html = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // Extract player response JSON
            val playerResponseMatch = Regex("var ytInitialPlayerResponse = (\\{.+?\\});").find(html)
            if (playerResponseMatch != null) {
                val jsonStr = playerResponseMatch.groupValues[1]
                val jsonObject = JSONObject(jsonStr)
                
                // Get streaming data
                if (jsonObject.has("streamingData")) {
                    val streamingData = jsonObject.getJSONObject("streamingData")
                    
                    // Try adaptive formats first (better quality)
                    if (streamingData.has("adaptiveFormats")) {
                        val formats = streamingData.getJSONArray("adaptiveFormats")
                        
                        // Find audio-only streams
                        for (i in 0 until formats.length()) {
                            val format = formats.getJSONObject(i)
                            
                            if (format.has("mimeType")) {
                                val mimeType = format.getString("mimeType")
                                
                                // Look for audio streams (webm or mp4)
                                if (mimeType.contains("audio")) {
                                    if (format.has("url")) {
                                        val audioUrl = format.getString("url")
                                        Log.d(TAG, "Found audio URL for video $videoId")
                                        return@withContext audioUrl
                                    }
                                }
                            }
                        }
                    }
                    
                    // Fallback to regular formats
                    if (streamingData.has("formats")) {
                        val formats = streamingData.getJSONArray("formats")
                        if (formats.length() > 0) {
                            val format = formats.getJSONObject(0)
                            if (format.has("url")) {
                                val audioUrl = format.getString("url")
                                Log.d(TAG, "Found fallback audio URL for video $videoId")
                                return@withContext audioUrl
                            }
                        }
                    }
                }
            }
            
            Log.w(TAG, "Could not extract audio URL for video $videoId")
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting audio URL for video $videoId", e)
            null
        }
    }
    
    /**
     * Parse ISO 8601 duration to seconds
     * Example: PT4M13S -> 253 seconds
     */
    fun parseDuration(isoDuration: String): Long {
        try {
            val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val match = regex.find(isoDuration)
            
            if (match != null) {
                val hours = match.groupValues[1].toLongOrNull() ?: 0
                val minutes = match.groupValues[2].toLongOrNull() ?: 0
                val seconds = match.groupValues[3].toLongOrNull() ?: 0
                
                return (hours * 3600 + minutes * 60 + seconds) * 1000 // Convert to milliseconds
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing duration: $isoDuration", e)
        }
        return 0L
    }
}
