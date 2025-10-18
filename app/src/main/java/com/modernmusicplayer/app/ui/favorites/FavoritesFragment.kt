package com.modernmusicplayer.app.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.databinding.FragmentFavoritesBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var songAdapter: SongAdapter
    private val favoriteSongs = mutableListOf<Song>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadFavorites()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh favorites when returning to this screen
        loadFavorites()
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicPlayerManager.playPlaylist(favoriteSongs, favoriteSongs.indexOf(song))
            },
            onMoreClick = { song ->
                // TODO: Show more options
            }
        )
        
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun loadFavorites() {
        val mainActivity = requireActivity() as MainActivity
        
        lifecycleScope.launch {
            mainActivity.musicRepository.getFavoriteSongsPersistent().collect { songs ->
                favoriteSongs.clear()
                favoriteSongs.addAll(songs)
                
                if (songs.isNotEmpty()) {
                    songAdapter.submitList(songs)
                    binding.favoritesRecyclerView.visibility = View.VISIBLE
                    binding.favoritesCount.text = "${songs.size} Favorites"
                    binding.emptyStateLayout.visibility = View.GONE
                } else {
                    binding.favoritesRecyclerView.visibility = View.GONE
                    binding.favoritesCount.text = "0 Favorites"
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
