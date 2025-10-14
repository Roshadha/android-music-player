package com.modernmusicplayer.app.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.data.repository.MusicRepository
import com.modernmusicplayer.app.databinding.FragmentSearchBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var musicRepository: MusicRepository
    private lateinit var songAdapter: SongAdapter
    
    private var searchJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        musicRepository = MusicRepository(requireContext())
        
        setupRecyclerView()
        setupSearchBar()
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                playSong(song)
            },
            onMoreClick = { song ->
                // Show more options for song
            }
        )
        
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun setupSearchBar() {
        // Language filter chips
        binding.chipEnglish.setOnClickListener { searchByLanguage("english") }
        binding.chipSinhala.setOnClickListener { searchByLanguage("sinhala") }
        binding.chipHindi.setOnClickListener { searchByLanguage("hindi") }
        binding.chipTamil.setOnClickListener { searchByLanguage("tamil") }
        binding.chipTelugu.setOnClickListener { searchByLanguage("telugu") }
        binding.chipKorean.setOnClickListener { searchByLanguage("kpop") }
        
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                binding.clearButton.isVisible = query.isNotEmpty()
                
                // Cancel previous search job
                searchJob?.cancel()
                
                // Start new search with debounce
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce delay
                    performSearch(query)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
        }
    }
    
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            showEmptyState()
            return
        }
        
        showLoading()
        
        lifecycleScope.launch {
            try {
                musicRepository.searchSongs(query).collect { songs ->
                    android.util.Log.d("SearchFragment", "Search results: ${songs.size} songs found")
                    updateUI(songs)
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchFragment", "Search error: ${e.message}", e)
                showNoResultsState()
            }
        }
    }
    
    private fun updateUI(songs: List<Song>) {
        if (songs.isEmpty()) {
            showNoResultsState()
        } else {
            showResults(songs)
        }
    }
    
    private fun showEmptyState() {
        binding.emptyStateLayout.isVisible = true
        binding.noResultsLayout.isVisible = false
        binding.searchRecyclerView.isVisible = false
        songAdapter.submitList(emptyList())
    }
    
    private fun showNoResultsState() {
        binding.emptyStateLayout.isVisible = false
        binding.noResultsLayout.isVisible = true
        binding.searchRecyclerView.isVisible = false
        songAdapter.submitList(emptyList())
    }
    
    private fun showResults(songs: List<Song>) {
        binding.emptyStateLayout.isVisible = false
        binding.noResultsLayout.isVisible = false
        binding.searchRecyclerView.isVisible = true
        songAdapter.submitList(songs)
    }
    
    private fun playSong(song: Song) {
        (activity as? MainActivity)?.musicPlayerManager?.playSong(song)
    }
    
    private fun searchByLanguage(language: String) {
        binding.searchEditText.text.clear()
        showLoading()
        
        lifecycleScope.launch {
            musicRepository.searchByLanguage(language).collect { songs ->
                if (songs.isNotEmpty()) {
                    showResults(songs)
                } else {
                    showNoResultsState()
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.emptyStateLayout.isVisible = false
        binding.noResultsLayout.isVisible = false
        binding.searchRecyclerView.isVisible = true
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}
