package com.modernmusicplayer.app.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.databinding.DialogFullPlayerBinding
import com.modernmusicplayer.app.service.MusicPlayerManager
import kotlinx.coroutines.launch

class FullPlayerDialog : DialogFragment() {
    
    private var _binding: DialogFullPlayerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var musicPlayerManager: MusicPlayerManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFullPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        musicPlayerManager = (requireActivity() as com.modernmusicplayer.app.ui.MainActivity).musicPlayerManager
        
        setupUI()
        observePlayer()
    }
    
    private fun setupUI() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        binding.playPauseButton.setOnClickListener {
            musicPlayerManager.togglePlayPause()
        }
        
        binding.nextButton.setOnClickListener {
            musicPlayerManager.playNext()
        }
        
        binding.previousButton.setOnClickListener {
            musicPlayerManager.playPrevious()
        }
        
        binding.shuffleButton.setOnClickListener {
            musicPlayerManager.toggleShuffle()
        }
        
        binding.repeatButton.setOnClickListener {
            musicPlayerManager.toggleRepeat()
        }
        
        binding.progressSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                val position = (value * musicPlayerManager.getDuration() / 100).toLong()
                musicPlayerManager.seekTo(position)
            }
        }
    }
    
    private fun observePlayer() {
        viewLifecycleOwner.lifecycleScope.launch {
            musicPlayerManager.currentSong.collect { song ->
                song?.let { updateSongInfo(it) }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            musicPlayerManager.isPlaying.collect { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            musicPlayerManager.currentPosition.collect { position ->
                updateProgress(position)
            }
        }
    }
    
    private fun updateSongInfo(song: Song) {
        binding.apply {
            songTitle.text = song.title
            artistName.text = song.artist
            songTitle.isSelected = true // Enable marquee
            
            Glide.with(this@FullPlayerDialog)
                .load(song.albumArtUrl)
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_music)
                .into(albumArtImage)
        }
    }
    
    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.playPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }
    
    private fun updateProgress(position: Long) {
        val duration = musicPlayerManager.getDuration()
        if (duration > 0) {
            val progress = (position.toFloat() / duration * 100).coerceIn(0f, 100f)
            binding.progressSlider.value = progress
            
            binding.currentTime.text = formatTime(position)
            binding.totalTime.text = formatTime(duration)
        }
    }
    
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
