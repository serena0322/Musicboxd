package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.network.Track

class HomeAdapter(
    private val trendTracks: List<Track>,
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<HomeAdapter.TrendViewHolder>() {

    override fun getItemCount() = 1 // Solo sezione Trend per ora

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)  // Usa il layout con rvSectionTracks e tvSectionTitle
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        holder.bind(trendTracks)
    }

    inner class TrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val recyclerViewTrend: RecyclerView = itemView.findViewById(R.id.rvSectionTracks)

        fun bind(tracks: List<Track>) {
            sectionTitle.text = "Trending Tracks"
            recyclerViewTrend.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            // Attenzione: TrackAdapter è ListAdapter, quindi usa submitList per aggiornare i dati
            val adapter = TrackAdapter3(onTrackClick, onTrackLongClick)
            recyclerViewTrend.adapter = adapter
            adapter.submitList(tracks)
        }
    }
}
