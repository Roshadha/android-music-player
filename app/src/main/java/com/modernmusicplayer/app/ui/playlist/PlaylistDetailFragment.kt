package com.modernmusicplayer.app.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.databinding.FragmentPlaylistDetailBinding
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import kotlinx.coroutines.launch

class PlaylistDetailFragment : Fragment() {
    
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var songAdapter: SongAdapter
    
    private val args: PlaylistDetailFragmentArgs by navArgs()
    private var playlistId: String = ""
    private var playlistName: String = ""
    private var playlistDescription: String = ""
    
    private val repository by lazy {
        (requireActivity() as MainActivity).musicRepository
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playlistId = args.playlistId
        playlistName = args.playlistName
        playlistDescription = args.playlistDescription ?: ""
        
        setupUI()
        setupRecyclerView()
        loadPlaylistSongs()
        setupListeners()
    }
    
    private fun setupUI() {
        binding.playlistName.text = playlistName
        if (playlistDescription.isNotEmpty()) {
            binding.playlistDescription.text = playlistDescription
            binding.playlistDescription.visibility = View.VISIBLE
        } else {
            binding.playlistDescription.visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                (requireActivity() as MainActivity).musicPlayerManager.playSong(song)
            },
            onMoreClick = { song ->
                showSongOptions(song)
            }
        )
        
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun loadPlaylistSongs() {
        lifecycleScope.launch {
            repository.getPlaylistSongs(playlistId).collect { songs: List<Song> ->
                songAdapter.submitList(songs)
                updateUI(songs)
            }
        }
    }
    
    private fun updateUI(songs: List<Song>) {
        val songCount = songs.size
        binding.songCount.text = if (songCount == 1) "1 song" else "$songCount songs"
        
        if (songs.isEmpty()) {
            binding.songsRecyclerView.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            binding.playAllButton.isEnabled = false
        } else {
            binding.songsRecyclerView.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            binding.playAllButton.isEnabled = true
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.playAllButton.setOnClickListener {
            val songs = songAdapter.currentList
            if (songs.isNotEmpty()) {
                (requireActivity() as MainActivity).musicPlayerManager.playSong(songs.first())
                Toast.makeText(requireContext(), "Playing all songs", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.editPlaylistButton.setOnClickListener {
            showEditPlaylistDialog()
        }
    }
    
    private fun showSongOptions(song: Song) {
        val popupMenu = PopupMenu(requireContext(), binding.root)
        popupMenu.inflate(R.menu.playlist_song_options_menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_remove_from_playlist -> {
                    removeSongFromPlaylist(song)
                    true
                }
                R.id.action_add_to_favorites -> {
                    lifecycleScope.launch {
                        repository.toggleFavoritePersistent(song.id)
                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_share -> {
                    // Share functionality
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    private fun removeSongFromPlaylist(song: Song) {
        lifecycleScope.launch {
            repository.removeSongFromPlaylist(playlistId, song.id)
            Toast.makeText(requireContext(), "Removed from playlist", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showEditPlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)
        
        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistNameEditText)
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistDescriptionEditText)
        
        nameInput.setText(playlistName)
        descriptionInput.setText(playlistDescription)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Playlist")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newDescription = descriptionInput.text.toString().trim()
                
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.updatePlaylist(playlistId, newName, newDescription)
                        playlistName = newName
                        playlistDescription = newDescription
                        setupUI()
                        Toast.makeText(requireContext(), "Playlist updated", Toast.LENGTH_SHORT).show()
                    }
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
