package com.modernmusicplayer.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modernmusicplayer.app.data.local.PreferencesManager
import com.modernmusicplayer.app.databinding.ItemPlaylistSelectorBinding

class PlaylistSelectorAdapter(
    private val playlists: List<PreferencesManager.PlaylistData>,
    private val onPlaylistClick: (PreferencesManager.PlaylistData) -> Unit
) : RecyclerView.Adapter<PlaylistSelectorAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistSelectorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistSelectorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: PreferencesManager.PlaylistData) {
            binding.apply {
                playlistName.text = playlist.name
                playlistSongCount.text = "${playlist.songIds.size} songs"
                root.setOnClickListener { onPlaylistClick(playlist) }
            }
        }
    }
}
