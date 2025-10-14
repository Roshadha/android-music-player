package com.modernmusicplayer.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.data.repository.MusicRepository
import com.modernmusicplayer.app.databinding.FragmentHomeBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import com.modernmusicplayer.app.ui.adapters.SongHorizontalAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val musicRepository by lazy { MusicRepository(requireContext()) }
    private lateinit var recentlyPlayedAdapter: SongHorizontalAdapter
    private lateinit var trendingAdapter: SongAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        loadContent()
    }
    
    private fun setupRecyclerViews() {
        // Recently Played
        recentlyPlayedAdapter = SongHorizontalAdapter { song ->
            playSong(song)
        }
        binding.recentlyPlayedRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyPlayedAdapter
        }
        
        // Trending
        trendingAdapter = SongAdapter(
            onSongClick = { song -> playSong(song) },
            onMoreClick = { song -> showMoreOptions(song) }
        )
        binding.trendingRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trendingAdapter
        }
    }
    
    private fun loadContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                android.util.Log.e("HomeFragment", "====== HOME FRAGMENT LOADING SONGS ======")
                musicRepository.getGlobalSongs().collect { songs ->
                    android.util.Log.e("HomeFragment", "====== RECEIVED ${songs.size} SONGS ======")
                    if (songs.isNotEmpty()) {
                        recentlyPlayedAdapter.submitList(songs.take(10))
                        trendingAdapter.submitList(songs)
                        android.util.Log.e("HomeFragment", "====== UI UPDATED WITH SONGS ======")
                    } else {
                        android.util.Log.e("HomeFragment", "====== NO SONGS AVAILABLE ======")
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Loading songs... Please check internet connection",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "====== ERROR LOADING SONGS ======", e)
                e.printStackTrace()
                android.widget.Toast.makeText(
                    requireContext(),
                    "Error loading songs. Check internet connection.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun playSong(song: Song) {
        (activity as? MainActivity)?.musicPlayerManager?.playSong(song)
    }
    
    private fun showMoreOptions(song: Song) {
        // Show bottom sheet with options
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
