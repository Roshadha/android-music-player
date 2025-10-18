package com.modernmusicplayer.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.modernmusicplayer.app.data.local.PreferencesManager
import com.modernmusicplayer.app.databinding.ItemPlaylistBinding
import java.text.SimpleDateFormat
import java.util.*

class PlaylistAdapter(
    private val onPlaylistClick: (PreferencesManager.PlaylistData) -> Unit,
    private val onMoreClick: (PreferencesManager.PlaylistData) -> Unit
) : ListAdapter<PreferencesManager.PlaylistData, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: PreferencesManager.PlaylistData) {
            binding.apply {
                playlistName.text = playlist.name
                songCount.text = "${playlist.songIds.size} songs"
                createdDate.text = formatDate(playlist.createdAt)

                root.setOnClickListener { onPlaylistClick(playlist) }
                moreButton.setOnClickListener { onMoreClick(playlist) }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 24 * 60 * 60 * 1000 -> "Created today"
                diff < 2 * 24 * 60 * 60 * 1000 -> "Created yesterday"
                diff < 7 * 24 * 60 * 60 * 1000 -> "Created ${diff / (24 * 60 * 60 * 1000)} days ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    "Created ${sdf.format(Date(timestamp))}"
                }
            }
        }
    }

    private class PlaylistDiffCallback : DiffUtil.ItemCallback<PreferencesManager.PlaylistData>() {
        override fun areItemsTheSame(oldItem: PreferencesManager.PlaylistData, newItem: PreferencesManager.PlaylistData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PreferencesManager.PlaylistData, newItem: PreferencesManager.PlaylistData): Boolean {
            return oldItem == newItem
        }
    }
}
