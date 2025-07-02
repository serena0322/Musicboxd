package com.example.musicboxd.adapter

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.classes.Track


//Usato per mostrare i dati dopo che sono stati trasformati da RemoteTrack a Track

class TrackResponse(
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, TrackResponse.TrackViewHolder>(DiffCallback()) {

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val artist = itemView.findViewById<TextView>(R.id.artist)
        private val cover = itemView.findViewById<ImageView>(R.id.cover)

        fun bind(track: Track, onClick: (Track) -> Unit) {
            title.text = track.title
            artist.text = track.artist?.name
            Glide.with(itemView.context).load(track.album?.cover).into(cover)

            itemView.setOnClickListener {
                onClick(track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position), onTrackClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}
