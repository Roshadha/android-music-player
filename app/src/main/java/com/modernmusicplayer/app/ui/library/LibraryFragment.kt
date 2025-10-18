package com.modernmusicplayer.app.ui.library

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.data.model.Song
import com.modernmusicplayer.app.databinding.FragmentLibraryBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.SongAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var playlistAdapter: com.modernmusicplayer.app.ui.adapters.PlaylistAdapter
    private lateinit var favoritesAdapter: SongAdapter
    
    private val localSongs = mutableListOf<Song>()
    private val playlists = mutableListOf<com.modernmusicplayer.app.data.local.PreferencesManager.PlaylistData>()
    private val favoriteSongs = mutableListOf<Song>()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scanLocalMusic()
        } else {
            Toast.makeText(requireContext(), "Permission required to scan music", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupPlaylistsSection()
        setupFavoritesSection()
        setupListeners()
        setupTabs()
        
        // Show local songs by default
        showLocalSongs()
        
        // Auto-scan local music on startup if permission granted
        checkAndAutoScanLocalMusic()
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showLocalSongs()
                    1 -> showPlaylists()
                    2 -> showFavorites()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    private fun showLocalSongs() {
        binding.localSongsSection.visibility = View.VISIBLE
        hidePlaylistsSection()
        hideFavoritesSection()
    }
    
    private fun showPlaylists() {
        binding.localSongsSection.visibility = View.GONE
        showPlaylistsSection()
        hideFavoritesSection()
        loadPlaylists()
    }
    
    private fun showFavorites() {
        binding.localSongsSection.visibility = View.GONE
        hidePlaylistsSection()
        showFavoritesSection()
        loadFavorites()
    }
    
    private fun checkAndAutoScanLocalMusic() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Scan silently on startup
            scanLocalMusic()
        }
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicPlayerManager.playPlaylist(localSongs, localSongs.indexOf(song))
            },
            onMoreClick = { song ->
                showMoreOptions(song)
            }
        )
        
        binding.localSongsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun setupListeners() {
        binding.scanButton.setOnClickListener {
            checkPermissionAndScan()
        }
    }
    
    private fun checkPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                scanLocalMusic()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun scanLocalMusic() {
        binding.scanButton.isEnabled = false
        binding.scanButton.text = "Scanning..."
        
        lifecycleScope.launch {
            try {
                val songs = withContext(Dispatchers.IO) {
                    getSongsFromStorage()
                }
                
                localSongs.clear()
                localSongs.addAll(songs)
                
                // Cache local music in repository for instant access
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicRepository.cacheLocalMusic(songs)
                
                if (songs.isNotEmpty()) {
                    songAdapter.submitList(songs)
                    binding.localSongsRecyclerView.visibility = View.VISIBLE
                    binding.localSongsCount.visibility = View.VISIBLE
                    binding.localSongsCount.text = "${songs.size} songs found"
                    binding.scanStorageCard.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.scanStorageCard.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error scanning music: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.scanButton.isEnabled = true
                binding.scanButton.text = "Scan Now"
            }
        }
    }
    
    private fun getSongsFromStorage(): List<Song> {
        val songs = mutableListOf<Song>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val query: Cursor? = requireContext().contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )
        
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                
                songs.add(
                    Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration, // in milliseconds
                        audioUrl = contentUri.toString(),
                        albumArtUrl = albumArtUri.toString(),
                        isLocal = true
                    )
                )
            }
        }
        
        return songs
    }
    
    private fun showMoreOptions(song: Song) {
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.root)
        popupMenu.menuInflater.inflate(R.menu.song_options_menu, popupMenu.menu)
        
        // Update menu item visibility based on favorite status
        lifecycleScope.launch {
            val mainActivity = requireActivity() as MainActivity
            val isFavorite = mainActivity.musicRepository.isFavoritePersistent(song.id)
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
    
    private fun showPlaylistSelector(song: Song) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_playlist, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        val mainActivity = requireActivity() as MainActivity
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.playlistsRecyclerView)
        val emptyText = dialogView.findViewById<android.widget.TextView>(R.id.emptyPlaylistsText)
        val createButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.createNewPlaylistButton)
        
        val playlists = mainActivity.musicRepository.getPlaylists()
        
        if (playlists.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            emptyText.visibility = android.view.View.VISIBLE
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyText.visibility = android.view.View.GONE
            
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            recyclerView.adapter = com.modernmusicplayer.app.ui.adapters.PlaylistSelectorAdapter(playlists) { playlist ->
                mainActivity.musicRepository.addSongToPlaylist(playlist.id, song.id)
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
        val mainActivity = requireActivity() as MainActivity
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val playlist = mainActivity.musicRepository.createPlaylist(name, description)
                    
                    // Add song if provided
                    song?.let {
                        mainActivity.musicRepository.addSongToPlaylist(playlist.id, it.id)
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
    
    // Playlists section methods
    private var playlistsRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var playlistsContainer: LinearLayout? = null
    private var playlistsFab: com.google.android.material.floatingactionbutton.FloatingActionButton? = null
    private var playlistsEmptyText: android.widget.TextView? = null
    
    private fun setupPlaylistsSection() {
        // Create playlists container if it doesn't exist
        if (playlistsContainer == null) {
            playlistsContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
                setPadding(16, 16, 16, 100) // Bottom padding for FAB
            }
            
            // Add title
            val titleText = android.widget.TextView(requireContext()).apply {
                text = "My Playlists"
                textSize = 20f
                setTextColor(android.graphics.Color.WHITE)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, 24)
            }
            playlistsContainer?.addView(titleText)
            
            // Add empty state
            playlistsEmptyText = android.widget.TextView(requireContext()).apply {
                text = "No playlists yet.\nTap the + button to create your first playlist!"
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                gravity = android.view.Gravity.CENTER
                setPadding(32, 100, 32, 32)
                visibility = View.GONE
            }
            playlistsContainer?.addView(playlistsEmptyText)
            
            // Add RecyclerView
            playlistsRecyclerView = androidx.recyclerview.widget.RecyclerView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutManager = LinearLayoutManager(requireContext())
            }
            playlistsContainer?.addView(playlistsRecyclerView)
            
            // Add FAB button
            playlistsFab = com.google.android.material.floatingactionbutton.FloatingActionButton(requireContext()).apply {
                val fabParams = androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                    setMargins(0, 0, 48, 48)
                }
                layoutParams = fabParams
                setImageResource(android.R.drawable.ic_input_add)
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#8B5CF6")
                )
                imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                setOnClickListener { showCreatePlaylistDialog() }
                visibility = View.GONE
            }
            
            // Add to parent layout
            val parent = binding.localSongsSection.parent as? ViewGroup
            parent?.addView(playlistsContainer)
            
            // Add FAB to root coordinator layout
            val rootLayout = binding.root.parent as? ViewGroup
            rootLayout?.addView(playlistsFab)
        }
        
        // Setup adapter
        playlistAdapter = com.modernmusicplayer.app.ui.adapters.PlaylistAdapter(
            onPlaylistClick = { playlist ->
                android.widget.Toast.makeText(
                    requireContext(),
                    "Opening ${playlist.name} - ${playlist.songIds.size} songs",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onMoreClick = { playlist ->
                showPlaylistOptions(playlist)
            }
        )
        playlistsRecyclerView?.adapter = playlistAdapter
    }
    
    private fun showPlaylistsSection() {
        playlistsContainer?.visibility = View.VISIBLE
        playlistsFab?.visibility = View.VISIBLE
    }
    
    private fun hidePlaylistsSection() {
        playlistsContainer?.visibility = View.GONE
        playlistsFab?.visibility = View.GONE
    }
    
    private fun loadPlaylists() {
        val mainActivity = requireActivity() as MainActivity
        playlists.clear()
        playlists.addAll(mainActivity.musicRepository.getPlaylists())
        
        if (playlists.isEmpty()) {
            playlistsRecyclerView?.visibility = View.GONE
            playlistsEmptyText?.visibility = View.VISIBLE
        } else {
            playlistsRecyclerView?.visibility = View.VISIBLE
            playlistsEmptyText?.visibility = View.GONE
        }
        
        playlistAdapter.submitList(playlists)
    }
    
    private fun showPlaylistOptions(playlist: com.modernmusicplayer.app.data.local.PreferencesManager.PlaylistData) {
        val options = arrayOf("Rename", "Delete")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(playlist.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenamePlaylistDialog(playlist)
                    1 -> showDeletePlaylistConfirmation(playlist)
                }
            }
            .show()
    }
    
    private fun showRenamePlaylistDialog(playlist: com.modernmusicplayer.app.data.local.PreferencesManager.PlaylistData) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val nameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistNameEditText)
        val descEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistDescriptionEditText)
        val mainActivity = requireActivity() as MainActivity
        
        nameEditText.setText(playlist.name)
        descEditText.setText(playlist.description)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Rename Playlist")
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    mainActivity.musicRepository.updatePlaylist(playlist.id, name, description)
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Playlist updated",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    loadPlaylists()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeletePlaylistConfirmation(playlist: com.modernmusicplayer.app.data.local.PreferencesManager.PlaylistData) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete \"${playlist.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicRepository.deletePlaylist(playlist.id)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Playlist deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                loadPlaylists()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    // Favorites section methods
    private var favoritesRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var favoritesContainer: LinearLayout? = null
    private var favoritesEmptyText: android.widget.TextView? = null
    private var favoritesCountText: android.widget.TextView? = null
    
    private fun setupFavoritesSection() {
        // Create favorites container if it doesn't exist
        if (favoritesContainer == null) {
            favoritesContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
                setPadding(16, 16, 16, 16)
            }
            
            // Add title
            val titleText = android.widget.TextView(requireContext()).apply {
                text = "Favorite Songs"
                textSize = 20f
                setTextColor(android.graphics.Color.WHITE)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, 0, 0, 8)
            }
            favoritesContainer?.addView(titleText)
            
            // Add count text
            favoritesCountText = android.widget.TextView(requireContext()).apply {
                text = "0 songs"
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                setPadding(0, 0, 0, 24)
            }
            favoritesContainer?.addView(favoritesCountText)
            
            // Add empty state
            favoritesEmptyText = android.widget.TextView(requireContext()).apply {
                text = "No favorite songs yet.\nTap the heart icon on any song to add it to your favorites!"
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                gravity = android.view.Gravity.CENTER
                setPadding(32, 100, 32, 32)
                visibility = View.GONE
            }
            favoritesContainer?.addView(favoritesEmptyText)
            
            // Add RecyclerView
            favoritesRecyclerView = androidx.recyclerview.widget.RecyclerView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutManager = LinearLayoutManager(requireContext())
            }
            favoritesContainer?.addView(favoritesRecyclerView)
            
            // Add to parent layout
            val parent = binding.localSongsSection.parent as? ViewGroup
            parent?.addView(favoritesContainer)
        }
        
        // Setup adapter
        favoritesAdapter = SongAdapter(
            onSongClick = { song ->
                val mainActivity = requireActivity() as MainActivity
                mainActivity.musicPlayerManager.playPlaylist(favoriteSongs, favoriteSongs.indexOf(song))
            },
            onMoreClick = { song ->
                showMoreOptions(song)
            }
        )
        favoritesRecyclerView?.adapter = favoritesAdapter
    }
    
    private fun showFavoritesSection() {
        favoritesContainer?.visibility = View.VISIBLE
    }
    
    private fun hideFavoritesSection() {
        favoritesContainer?.visibility = View.GONE
    }
    
    private fun loadFavorites() {
        val mainActivity = requireActivity() as MainActivity
        lifecycleScope.launch {
            mainActivity.musicRepository.getFavoriteSongsPersistent().collect { songs ->
                favoriteSongs.clear()
                favoriteSongs.addAll(songs)
                
                if (songs.isEmpty()) {
                    favoritesRecyclerView?.visibility = View.GONE
                    favoritesEmptyText?.visibility = View.VISIBLE
                    favoritesCountText?.text = "0 songs"
                } else {
                    favoritesRecyclerView?.visibility = View.VISIBLE
                    favoritesEmptyText?.visibility = View.GONE
                    favoritesCountText?.text = "${songs.size} ${if (songs.size == 1) "song" else "songs"}"
                }
                
                favoritesAdapter.submitList(songs)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
