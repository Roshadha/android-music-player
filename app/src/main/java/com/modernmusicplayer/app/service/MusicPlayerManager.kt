package com.modernmusicplayer.app.service

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.modernmusicplayer.app.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicPlayerManager(private val context: Context) {
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist
    
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex
    
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode
    
    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlayerService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = mediaController?.duration ?: 0L
                }
            }
        })
    }
    
    fun playSong(song: Song) {
        _currentSong.value = song
        
        // Track recently played
        val repository = com.modernmusicplayer.app.data.repository.MusicRepository(context)
        repository.addRecentlyPlayed(song.id)
        
        // Check if we need to extract audio URL using Piped
        if (song.audioUrl.startsWith("piped://")) {
            // Extract video ID and get real audio URL
            val videoId = song.audioUrl.removePrefix("piped://")
            
            // Extract audio URL in background
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Log.d("MusicPlayerManager", "Getting audio for video: $videoId")
                    val pipedApi = com.modernmusicplayer.app.data.api.ApiClient.pipedApi
                    val streams = withContext(Dispatchers.IO) {
                        pipedApi.getStreams(videoId)
                    }
                    
                    // Find best audio stream (prefer highest bitrate)
                    val audioStream = streams.audioStreams
                        .filter { it.format == "M4A" || it.format == "WEBMA_OPUS" }
                        .maxByOrNull { it.bitrate }
                    
                    if (audioStream != null && audioStream.url.isNotEmpty()) {
                        Log.d("MusicPlayerManager", "✓ Audio URL found: ${audioStream.quality} ${audioStream.format}")
                        val mediaItem = MediaItem.Builder()
                            .setUri(audioStream.url)
                            .setMediaId(song.id)
                            .build()
                        
                        mediaController?.apply {
                            setMediaItem(mediaItem)
                            prepare()
                            play()
                        }
                    } else {
                        Log.e("MusicPlayerManager", "✗ No audio stream found for video")
                    }
                } catch (e: Exception) {
                    Log.e("MusicPlayerManager", "✗ Error getting YouTube audio", e)
                }
            }
        } else {
            // Regular audio URL
            val mediaItem = MediaItem.Builder()
                .setUri(song.audioUrl)
                .setMediaId(song.id)
                .build()
            
            mediaController?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        _playlist.value = songs
        _currentIndex.value = startIndex
        
        // For yt-dlp songs, we'll play them one at a time as URLs are extracted
        // For now, just play the first song
        if (songs.isNotEmpty()) {
            _currentSong.value = songs[startIndex]
            playSong(songs[startIndex])
        }
    }
    
    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    fun playPause() {
        togglePlayPause()
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun playNext() {
        mediaController?.seekToNext()
        updateCurrentSongFromIndex()
    }
    
    fun next() {
        playNext()
    }
    
    fun previous() {
        mediaController?.seekToPrevious()
        updateCurrentSongFromIndex()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun toggleShuffle() {
        val newShuffleMode = !_shuffleEnabled.value
        _shuffleEnabled.value = newShuffleMode
        mediaController?.shuffleModeEnabled = newShuffleMode
    }
    
    fun toggleRepeat() {
        val newMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = newMode
        mediaController?.repeatMode = newMode
    }
    
    fun toggleRepeatMode() {
        toggleRepeat()
    }
    
    fun playPrevious() {
        previous()
    }
    
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }
    
    private fun updateCurrentSongFromIndex() {
        mediaController?.let { controller ->
            val index = controller.currentMediaItemIndex
            _currentIndex.value = index
            if (index >= 0 && index < _playlist.value.size) {
                _currentSong.value = _playlist.value[index]
            }
        }
    }
    
    fun release() {
        mediaController?.release()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
