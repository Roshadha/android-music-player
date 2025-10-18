package com.modernmusicplayer.app.ui.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.databinding.FragmentRecentlyPlayedBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import kotlinx.coroutines.launch

class RecentlyPlayedFragment : Fragment() {
    
    private var _binding: FragmentRecentlyPlayedBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var songAdapter: SongAdapter
    private val recentSongs = mutableListOf<Song>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentlyPlayedBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
        loadRecentlyPlayed()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh recent tracks when returning to this screen
        loadRecentlyPlayed()
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicPlayerManager.playPlaylist(recentSongs, recentSongs.indexOf(song))
            },
            onMoreClick = { song ->
                // TODO: Show more options
            }
        )
        
        binding.recentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun setupListeners() {
        binding.clearButton.setOnClickListener {
            showClearConfirmDialog()
        }
    }
    
    private fun showClearConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all recently played tracks?")
            .setPositiveButton("Clear") { _, _ ->
                clearHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearHistory() {
        val mainActivity = requireActivity() as MainActivity
        val prefsManager = com.modernmusicplayer.app.data.local.PreferencesManager(requireContext())
        prefsManager.clearRecentTracks()
        loadRecentlyPlayed()
    }
    
    private fun loadRecentlyPlayed() {
        val mainActivity = requireActivity() as MainActivity
        
        lifecycleScope.launch {
            mainActivity.musicRepository.getRecentlyPlayed().collect { songs ->
                recentSongs.clear()
                recentSongs.addAll(songs)
                
                if (songs.isNotEmpty()) {
                    songAdapter.submitList(songs)
                    binding.recentRecyclerView.visibility = View.VISIBLE
                    binding.recentCount.text = "${songs.size} Tracks"
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.clearButton.visibility = View.VISIBLE
                } else {
                    binding.recentRecyclerView.visibility = View.GONE
                    binding.recentCount.text = "0 Tracks"
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.clearButton.visibility = View.GONE
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
