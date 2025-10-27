package com.example.musicboxd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.network.Track

// Data class per ogni sezione
data class TrackSection(
    val title: String,
    val tracks: List<Track>
)

// Adapter principale della schermata Home: mostra più sezioni (es. “Top Songs”, “Nuove Uscite”),
// ognuna con una lista orizzontale di brani gestita da TrackAdapter3.

class HomeAdapter(
    private val sections: List<TrackSection>,
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<HomeAdapter.SectionViewHolder>() {

    override fun getItemCount() = sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false) // Layout con tvSectionTitle e rvSectionTracks
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rvSectionTracks)

        fun bind(section: TrackSection) {
            sectionTitle.text = section.title

            recyclerView.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            val adapter = TrackAdapter3(onTrackClick, onTrackLongClick)
            recyclerView.adapter = adapter
            adapter.submitList(section.tracks)
        }
    }
}