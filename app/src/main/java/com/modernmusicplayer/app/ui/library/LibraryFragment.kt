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
    private val localSongs = mutableListOf<Song>()
    
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
        setupListeners()
        
        // Auto-scan local music on startup if permission granted
        checkAndAutoScanLocalMusic()
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
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Add to playlist coming soon!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
