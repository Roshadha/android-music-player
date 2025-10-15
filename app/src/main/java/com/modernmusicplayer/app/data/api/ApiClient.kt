package com.modernmusicplayer.app.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val DEEZER_BASE_URL = "https://api.deezer.com/"
    private const val JIOSAAVN_BASE_URL = "https://saavn.dev/api/"
    private const val JAMENDO_BASE_URL = "https://api.jamendo.com/v3.0/"
    private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private const val PIPED_BASE_URL = "https://pipedapi.adminforge.de/" // Piped - German instance
    private const val ITUNES_BASE_URL = "https://itunes.apple.com/" // iTunes API - No key needed!
    
    // API Keys (public, no authentication needed)
    const val JAMENDO_CLIENT_ID = "56d30c95"
    const val YOUTUBE_API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8" // Public YouTube Data API key
    
    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }
    
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(getLoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    val deezerApi: MusicApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DEEZER_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApiService::class.java)
    }
    
    val jioSaavnApi: JioSaavnApiService by lazy {
        Retrofit.Builder()
            .baseUrl(JIOSAAVN_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JioSaavnApiService::class.java)
    }
    
    val jamendoApi: JamendoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(JAMENDO_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JamendoApiService::class.java)
    }
    
    val youtubeApi: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(YOUTUBE_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }
    
    val invidiousApi: InvidiousApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://invidious.jing.rocks/")
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InvidiousApiService::class.java)
    }
    
    val pipedApi: PipedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PIPED_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PipedApiService::class.java)
    }
    
    val itunesApi: ITunesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApiService::class.java)
    }
}
