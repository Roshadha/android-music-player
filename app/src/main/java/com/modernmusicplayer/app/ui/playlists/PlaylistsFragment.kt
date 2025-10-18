package com.modernmusicplayer.app.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.data.local.PreferencesManager
import com.modernmusicplayer.app.databinding.FragmentPlaylistsBinding
import com.modernmusicplayer.app.ui.MainActivity
import com.modernmusicplayer.app.ui.adapters.PlaylistAdapter

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistAdapter
    private val playlists = mutableListOf<PreferencesManager.PlaylistData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB()
        loadPlaylists()
    }

    override fun onResume() {
        super.onResume()
        loadPlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                // TODO: Navigate to playlist detail screen
                android.widget.Toast.makeText(
                    requireContext(),
                    "Opening ${playlist.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onMoreClick = { playlist ->
                showPlaylistOptions(playlist)
            }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }
    }

    private fun setupFAB() {
        binding.fabCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun loadPlaylists() {
        val mainActivity = requireActivity() as MainActivity
        playlists.clear()
        playlists.addAll(mainActivity.musicRepository.getPlaylists())

        if (playlists.isNotEmpty()) {
            playlistAdapter.submitList(playlists)
            binding.playlistsRecyclerView.visibility = View.VISIBLE
            binding.playlistCount.text = "${playlists.size} Playlists"
            binding.emptyStateLayout.visibility = View.GONE
        } else {
            binding.playlistsRecyclerView.visibility = View.GONE
            binding.playlistCount.text = "0 Playlists"
            binding.emptyStateLayout.visibility = View.VISIBLE
        }
    }

    private fun showCreatePlaylistDialog() {
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
                    mainActivity.musicRepository.createPlaylist(name, description)
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Playlist \"$name\" created",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    loadPlaylists()
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

    private fun showPlaylistOptions(playlist: PreferencesManager.PlaylistData) {
        val options = arrayOf("Rename", "Delete")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(playlist.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(playlist)
                    1 -> showDeleteConfirmation(playlist)
                }
            }
            .show()
    }

    private fun showRenameDialog(playlist: PreferencesManager.PlaylistData) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val nameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistNameEditText)
        val descEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playlistDescriptionEditText)
        val mainActivity = requireActivity() as MainActivity

        // Pre-fill with existing data
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

    private fun showDeleteConfirmation(playlist: PreferencesManager.PlaylistData) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
