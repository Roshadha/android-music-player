package com.modernmusicplayer.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.R
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
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.root)
        popupMenu.menuInflater.inflate(R.menu.song_options_menu, popupMenu.menu)
        
        // Update menu item visibility based on favorite status
        viewLifecycleOwner.lifecycleScope.launch {
            val isFavorite = musicRepository.isFavoritePersistent(song.id)
            popupMenu.menu.findItem(R.id.action_add_to_favorites)?.isVisible = !isFavorite
            popupMenu.menu.findItem(R.id.action_remove_from_favorites)?.isVisible = isFavorite
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_add_to_favorites -> {
                        addToFavorites(song)
                        true
                    }
                    R.id.action_remove_from_favorites -> {
                        removeFromFavorites(song)
                        true
                    }
                    R.id.action_add_to_playlist -> {
                        showPlaylistSelector(song)
                        true
                    }
                    R.id.action_share -> {
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
        viewLifecycleOwner.lifecycleScope.launch {
            musicRepository.toggleFavoritePersistent(song.id)
            android.widget.Toast.makeText(
                requireContext(),
                "Added to Favorites",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun removeFromFavorites(song: Song) {
        viewLifecycleOwner.lifecycleScope.launch {
            musicRepository.toggleFavoritePersistent(song.id)
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
    
    private fun showPlaylistSelector(song: Song) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_playlist, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.playlistsRecyclerView)
        val emptyText = dialogView.findViewById<android.widget.TextView>(R.id.emptyPlaylistsText)
        val createButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.createNewPlaylistButton)
        
        val playlists = musicRepository.getPlaylists()
        
        if (playlists.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            emptyText.visibility = android.view.View.VISIBLE
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyText.visibility = android.view.View.GONE
            
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            recyclerView.adapter = com.modernmusicplayer.app.ui.adapters.PlaylistSelectorAdapter(playlists) { playlist ->
                musicRepository.addSongToPlaylist(playlist.id, song.id)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Added to ${playlist.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }
        
        createButton.setOnClickListener {
            dialog.dismiss()
            showCreatePlaylistDialog(song)
        }
        
        dialog.show()
    }
    
    private fun showCreatePlaylistDialog(song: Song? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val nameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistNameEditText)
        val descEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistDescriptionEditText)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val playlist = musicRepository.createPlaylist(name, description)
                    
                    // Add song if provided
                    song?.let {
                        musicRepository.addSongToPlaylist(playlist.id, it.id)
                    }
                    
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Playlist created${if (song != null) " and song added" else ""}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter a playlist name",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
