package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.network.Track
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class InformationActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val track = intent.getParcelableExtra<Track>("track")

        val title = track?.title
        val artist = intent.getStringExtra("artist")
        val albumTitle = track?.album?.title
        val duration = track?.duration
        val releaseDate = track?.album?.release_date
        val lyrics = track?.explicit_lyrics
        val preview = track?.preview
        val rank = track?.rank
        val coverUrl = intent.getStringExtra("cover")

        findViewById<TextView>(R.id.textView8).text = "🎧 Titolo: ${title ?: "N/A"}"
        findViewById<TextView>(R.id.textView9).text = "👤 Artista: ${artist ?: "N/A"}"
        findViewById<TextView>(R.id.textView7).text = "Album: ${albumTitle ?: "N/A"}"
        findViewById<TextView>(R.id.textView10).text = "Durata: ${duration?.toString() ?: "N/A"} sec"
        findViewById<TextView>(R.id.textView11).text = "Data: ${releaseDate ?: "N/A"}"
        findViewById<TextView>(R.id.textView14).text = "Explicit: ${lyrics ?: "N/A"}"
        findViewById<TextView>(R.id.textView12).text = "Rank: ${rank?.toString() ?: "N/A"}"
        val coverImageView = findViewById<ImageView>(R.id.infoCover)
        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person) // immagine di default durante il caricamento
            .error(R.drawable.person)       // immagine se il caricamento fallisce
            .into(coverImageView)

        val playButton = findViewById<Button>(R.id.button3)

        playButton.setOnClickListener {
            if (!isPlaying && preview != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(preview)
                    prepare()
                    start()
                }
                isPlaying = true
                playButton.text = "⏸️ Stop"

                mediaPlayer?.setOnCompletionListener {
                    playButton.text = "▶️ Preview"
                    isPlaying = false
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                playButton.text = "▶️ Preview"
                isPlaying = false
            }
        }

        val playlist = findViewById<Button>(R.id.playlist)
        playlist.setOnClickListener {
            AddToPlaylist(track)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun AddToPlaylist(track: Track?) {
        if (track == null) return

        val context = this

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("PLAYLIST_DEBUG", "Current userId: $userId")
        val db = FirebaseFirestore.getInstance()

        // Recupera tutte le playlist dell’utente
        db.collection("User")
            .document(userId)
            .collection("Playlists")
            .get()
            .addOnSuccessListener { result ->
                Log.d("PLAYLIST_DEBUG", "Documenti trovati: ${result.size()}")
                for (doc in result.documents) {
                    Log.d(
                        "PLAYLIST_DEBUG",
                        "ID: ${doc.id}, name=${doc.get("name")}, createdBy=${doc.get("createdBy")}"
                    )
                }

                val playlists = result.documents.mapNotNull { it.getString("name") }

                if (playlists.isEmpty()) {
                    Toast.makeText(context, "Non hai ancora playlist", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                AlertDialog.Builder(context)
                    .setTitle("Aggiungi a playlist")
                    .setItems(playlists.toTypedArray()) { _, which ->
                        val selectedPlaylist = playlists[which]
                        addTrackToPlaylist(userId, selectedPlaylist, track)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Errore nel recupero delle playlist", Toast.LENGTH_SHORT)
                    .show()
            }
    }


        private fun addTrackToPlaylist(userId: String, playlistName: String, track: Track) {
        val db = FirebaseFirestore.getInstance()

        // Identifica la playlist corretta dell’utente
        db.collection("User")
            .document(userId)
            .collection("Playlists")
            .whereEqualTo("createdBy", userId)
            .whereEqualTo("name", playlistName)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener

                val playlistDoc = result.documents[0].reference

                // Aggiunge il titolo (o ID) del brano al campo tracks (array union)
                playlistDoc.update("tracks", FieldValue.arrayUnion(track.id?.toString() ?: "ID non disponibile"))
                    .addOnSuccessListener {
                        Log.d("Playlist", "Brano aggiunto con successo")
                        Toast.makeText(
                            this@InformationActivity,
                            "${track.title} aggiunta a $playlistName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Playlist", "Errore nell'aggiunta del brano", e)
                    }
            }
    }

}