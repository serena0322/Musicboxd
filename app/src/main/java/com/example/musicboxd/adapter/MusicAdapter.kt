package com.example.musicboxd.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.MusicItem
import com.example.musicboxd.R

class MusicAdapter(private val items: List<MusicItem>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.musicTitle)
        val artist: TextView = view.findViewById(R.id.musicArtist)
        val row: View = view.findViewById(R.id.row)
    }

    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.drawable.item_music, parent, false)
        var holder= MusicViewHolder(view)
        holder.row.setOnClickListener{
            //al click
        }
        return holder
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.artist.text = item.artist
    }

    override fun getItemCount(): Int = items.size
}

