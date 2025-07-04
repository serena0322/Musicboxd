package com.example.musicboxd.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.classes.Image
import com.example.musicboxd.local.Album
import com.example.musicboxd.local.Artist
import com.example.musicboxd.local.Song

//componente che consente di mostrare una lista di canzoni (song) in modo efficiente e dinamico

class SongAdapter(
    private val songs: MutableList<Song>,
    private var artistMap: Map<String, Artist>,
    private var albumMap: Map<String, Album>,
    private var albumImagesMap: Map<String, List<Image>>
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.songTitle)
        val artist: TextView = view.findViewById(R.id.songArtist)
        val image: ImageView = view.findViewById(R.id.albumCover)
        val album: TextView = view.findViewById(R.id.albumName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val artist1 = artistMap[song.artistId]
        val album1 = albumMap[song.albumId]
        val images1 = albumImagesMap[song.albumId]
        val imageUrl1 = images1?.firstOrNull()?.url

        Log.d("SongAdapter", "Song: ${song.title}, artistId: ${song.artistId}")
        Log.d("SongAdapter", "onBindViewHolder position: $position")
        Log.d("SongAdapter", "Title: ${song.title ?: "null"}")
        Log.d("SongAdapter", "Artist: ${artist1?.name ?: "null"}")
        Log.d("SongAdapter", "Album: ${album1?.title ?: "null"}")
        Log.d("SongAdapter", "Image url: $imageUrl1")


        // Titolo canzone (se previsto)
        holder.title.text = song.title ?: "Titolo sconosciuto"



        val artist = artistMap[song.artistId]
        if (artist == null) {
            Log.w("SongAdapter", "Artista non trovato per artistId: ${song.artistId}")
        }
        holder.artist.text = artist?.name ?: "Artista sconosciuto"

        val album = albumMap[song.albumId]
        holder.album.text = album?.title ?: "Album sconosciuto"

        val images = albumImagesMap[song.albumId]
        val imageUrl = images?.firstOrNull()?.url

        Glide.with(holder.image.context)
            .load(imageUrl ?: R.drawable.music_album)
            .into(holder.image)
    }

    override fun getItemCount() = songs.size

    // Metodo per aggiornare la mappa degli artisti
    fun updateArtistMap(newArtistMap: Map<String, Artist>) {
        artistMap = newArtistMap
        notifyDataSetChanged()
    }

    // Metodo per aggiornare la mappa degli album (opzionale)
    fun updateAlbumMap(newAlbumMap: Map<String, Album>) {
        albumMap = newAlbumMap
        notifyDataSetChanged()
    }

    // Metodo per aggiornare la mappa delle immagini (opzionale)
    fun updateAlbumImagesMap(newAlbumImagesMap: Map<String, List<Image>>) {
        albumImagesMap = newAlbumImagesMap
        notifyDataSetChanged()
    }
}
