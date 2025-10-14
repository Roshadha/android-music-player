package com.modernmusicplayer.app.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.databinding.FragmentPlayerBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.data.model.formatDuration
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    
    private val musicPlayerManager by lazy {
        (activity as MainActivity).musicPlayerManager
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observePlayerState()
    }
    
    private fun setupClickListeners() {
        binding.apply {
            closeButton.setOnClickListener {
                // Navigate back or close
            }
            
            playPauseButton.setOnClickListener {
                musicPlayerManager.playPause()
            }
            
            nextButton.setOnClickListener {
                musicPlayerManager.next()
            }
            
            previousButton.setOnClickListener {
                musicPlayerManager.previous()
            }
            
            shuffleButton.setOnClickListener {
                musicPlayerManager.toggleShuffle()
            }
            
            repeatButton.setOnClickListener {
                musicPlayerManager.toggleRepeatMode()
            }
            
            favoriteButton.setOnClickListener {
                // Toggle favorite
            }
            
            progressBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        musicPlayerManager.seekTo(progress.toLong())
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }
    }
    
    private fun observePlayerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            musicPlayerManager.currentSong.collect { song ->
                song?.let {
                    binding.apply {
                        songTitle.text = it.title
                        artistName.text = it.artist
                        totalTime.text = it.duration.formatDuration()
                        
                        Glide.with(requireContext())
                            .load(it.albumArtUrl)
                            .placeholder(R.drawable.ic_home)
                            .into(albumArt)
                        
                        progressBar.max = it.duration.toInt()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            musicPlayerManager.isPlaying.collect { isPlaying ->
                binding.playPauseButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
