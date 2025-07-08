package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicboxd.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.local.PlaylistItem

class PlaylistAdapter(
    private val playlists: MutableList<PlaylistItem>,
    private val onItemClick: (PlaylistItem) -> Unit,
    private val onLongClick: (PlaylistItem) -> Unit) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.playlistName)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(playlists[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        holder.itemView.setOnLongClickListener {
            onLongClick(playlist)
            true
        }
        holder.nameTextView.text = playlist.name
    }



    override fun getItemCount(): Int = playlists.size

    fun addPlaylist(playlist: PlaylistItem) {
        playlists.add(0, playlist) // aggiunta in cima
        notifyItemInserted(0)
    }
}

