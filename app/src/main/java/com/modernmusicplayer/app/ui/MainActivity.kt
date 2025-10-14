package com.modernmusicplayer.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.databinding.ActivityMainBinding
import com.modernmusicplayer.app.service.MusicPlayerManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    lateinit var musicPlayerManager: MusicPlayerManager
        private set
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Music Player
        musicPlayerManager = MusicPlayerManager(this)
        musicPlayerManager.initialize()
        
        setupNavigation()
        setupMiniPlayer()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun setupMiniPlayer() {
        lifecycleScope.launch {
            musicPlayerManager.currentSong.collect { song ->
                if (song != null) {
                    binding.miniPlayer.root.visibility = android.view.View.VISIBLE
                    updateMiniPlayer(song)
                } else {
                    binding.miniPlayer.root.visibility = android.view.View.GONE
                }
            }
        }
        
        // Mini player controls
        binding.miniPlayer.playPauseButton.setOnClickListener {
            musicPlayerManager.togglePlayPause()
        }
        
        binding.miniPlayer.nextButton.setOnClickListener {
            musicPlayerManager.playNext()
        }
        
        binding.miniPlayer.root.setOnClickListener {
            // Open full player dialog
            val fullPlayerDialog = com.modernmusicplayer.app.ui.player.FullPlayerDialog()
            fullPlayerDialog.show(supportFragmentManager, "FullPlayerDialog")
        }
        
        // Update play/pause button state
        lifecycleScope.launch {
            musicPlayerManager.isPlaying.collect { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }
    }
    
    private fun updateMiniPlayer(song: com.modernmusicplayer.app.data.model.Song) {
        binding.miniPlayer.apply {
            songTitle.text = song.title
            artistName.text = song.artist
            
            // Load album art
            com.bumptech.glide.Glide.with(this@MainActivity)
                .load(song.albumArtUrl)
                .placeholder(R.drawable.ic_home)
                .into(albumArt)
        }
    }
    
    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.miniPlayer.playPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::musicPlayerManager.isInitialized) {
            musicPlayerManager.release()
        }
    }
}
