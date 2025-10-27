package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.network.Track

// Adapter per la lista dei brani: mostra titolo, artista e copertina, gestendo click e long click
// su ciascun elemento con aggiornamento efficiente tramite DiffUtil.

class TrackAdapter3(
    private val onItemClick: (Track) -> Unit,
    private val onLongClick: (Track) -> Unit
) : ListAdapter<Track, TrackAdapter3.TrackViewHolder>(DiffCallback()) {

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val artist: TextView = itemView.findViewById(R.id.artist)
        private val cover: ImageView = itemView.findViewById(R.id.cover)

        fun bind(track: Track) {
            title.text = track.title
            artist.text = track.artist?.name
            Glide.with(itemView.context)
                .load(track.album?.cover)
                //.placeholder(R.drawable.ic_music_placeholder)
                .into(cover)

            itemView.setOnClickListener { onItemClick(track) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track2, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))

        val track = getItem(position)

        holder.itemView.setOnLongClickListener {
            onLongClick(track)
            true
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.artist?.name == newItem.artist?.name
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}
