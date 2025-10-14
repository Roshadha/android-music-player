package com.modernmusicplayer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class MusicPlayerApplication : Application() {
    
    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val CHANNEL_NAME = "Music Playback"
        private const val TAG = "MusicPlayerApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.e(TAG, "============================================")
        Log.e(TAG, "MUSIC APP STARTED - Using Piped for YouTube")
        Log.e(TAG, "============================================")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music player controls and notifications"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
