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
                showMoreOptions(song)
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
    
    private fun showMoreOptions(song: Song) {
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.root)
        popupMenu.menuInflater.inflate(com.modernmusicplayer.app.R.menu.song_options_menu, popupMenu.menu)
        
        // Update menu item visibility based on favorite status
        lifecycleScope.launch {
            val mainActivity = requireActivity() as MainActivity
            val isFavorite = mainActivity.musicRepository.isFavoritePersistent(song.id)
            popupMenu.menu.findItem(com.modernmusicplayer.app.R.id.action_add_to_favorites)?.isVisible = !isFavorite
            popupMenu.menu.findItem(com.modernmusicplayer.app.R.id.action_remove_from_favorites)?.isVisible = isFavorite
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    com.modernmusicplayer.app.R.id.action_add_to_favorites -> {
                        addToFavorites(song)
                        true
                    }
                    com.modernmusicplayer.app.R.id.action_remove_from_favorites -> {
                        removeFromFavorites(song)
                        true
                    }
                    com.modernmusicplayer.app.R.id.action_add_to_playlist -> {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Add to playlist coming soon!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    com.modernmusicplayer.app.R.id.action_share -> {
                        shareSong(song)
                        true
                    }
                    else -> false
                }
            }
            
            popupMenu.show()
        }
    }
    
    private fun addToFavorites(song: Song) {
        lifecycleScope.launch {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.musicRepository.toggleFavoritePersistent(song.id)
            android.widget.Toast.makeText(
                requireContext(),
                "Added to Favorites",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun removeFromFavorites(song: Song) {
        lifecycleScope.launch {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.musicRepository.toggleFavoritePersistent(song.id)
            android.widget.Toast.makeText(
                requireContext(),
                "Removed from Favorites",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun shareSong(song: Song) {
        val shareText = "Check out this song: ${song.title} by ${song.artist}"
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share song via"))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
