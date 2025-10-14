package com.modernmusicplayer.app.utils

import android.content.Context
import android.util.Log
import com.modernmusicplayer.app.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility to test API connectivity
 */
object ApiTester {
    
    private const val TAG = "ApiTester"
    
    suspend fun testAllApis(context: Context): String {
        val results = StringBuilder()
        
        results.append("=== API Connection Test ===\n\n")
        
        // Test YouTube API
        results.append("Testing YouTube API...\n")
        try {
            val youtubeResponse = withContext(Dispatchers.IO) {
                ApiClient.youtubeApi.searchVideos(
                    query = "test music",
                    apiKey = ApiClient.YOUTUBE_API_KEY,
                    maxResults = 1
                )
            }
            
            if (youtubeResponse.isSuccessful) {
                val items = youtubeResponse.body()?.items?.size ?: 0
                results.append("✅ YouTube API: SUCCESS ($items results)\n")
                Log.d(TAG, "YouTube API working: $items results")
            } else {
                results.append("❌ YouTube API: FAILED (${youtubeResponse.code()} - ${youtubeResponse.message()})\n")
                Log.e(TAG, "YouTube API failed: ${youtubeResponse.code()} - ${youtubeResponse.message()}")
            }
        } catch (e: Exception) {
            results.append("❌ YouTube API: ERROR (${e.message})\n")
            Log.e(TAG, "YouTube API error", e)
        }
        
        results.append("\n")
        
        // Test Jamendo API
        results.append("Testing Jamendo API...\n")
        try {
            val jamendoResponse = withContext(Dispatchers.IO) {
                ApiClient.jamendoApi.getTracks(
                    clientId = ApiClient.JAMENDO_CLIENT_ID,
                    limit = 1
                )
            }
            
            if (jamendoResponse.isSuccessful) {
                val tracks = jamendoResponse.body()?.results?.size ?: 0
                results.append("✅ Jamendo API: SUCCESS ($tracks tracks)\n")
                Log.d(TAG, "Jamendo API working: $tracks tracks")
            } else {
                results.append("❌ Jamendo API: FAILED (${jamendoResponse.code()} - ${jamendoResponse.message()})\n")
                Log.e(TAG, "Jamendo API failed: ${jamendoResponse.code()} - ${jamendoResponse.message()}")
            }
        } catch (e: Exception) {
            results.append("❌ Jamendo API: ERROR (${e.message})\n")
            Log.e(TAG, "Jamendo API error", e)
        }
        
        results.append("\n")
        
        // Test Deezer API
        results.append("Testing Deezer API...\n")
        try {
            val deezerResponse = withContext(Dispatchers.IO) {
                ApiClient.deezerApi.searchDeezer(
                    query = "test",
                    limit = 1
                )
            }
            
            if (deezerResponse.isSuccessful) {
                val data = deezerResponse.body()?.data?.size ?: 0
                results.append("✅ Deezer API: SUCCESS ($data results)\n")
                Log.d(TAG, "Deezer API working: $data results")
            } else {
                results.append("❌ Deezer API: FAILED (${deezerResponse.code()} - ${deezerResponse.message()})\n")
                Log.e(TAG, "Deezer API failed: ${deezerResponse.code()} - ${deezerResponse.message()}")
            }
        } catch (e: Exception) {
            results.append("❌ Deezer API: ERROR (${e.message})\n")
            Log.e(TAG, "Deezer API error", e)
        }
        
        results.append("\n=== Test Complete ===")
        
        return results.toString()
    }
}
