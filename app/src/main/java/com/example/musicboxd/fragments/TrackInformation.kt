package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.network.Track
import com.example.musicboxd.network.RetrofitInstance.api
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TrackInformation : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val track = intent.getParcelableExtra<Track>("track")

        val title = track?.title
        val artist = track?.artist?.name
        val albumTitle = track?.album?.title
        val duration = track?.duration
        val preview = track?.preview
        val coverUrl = intent.getStringExtra("cover")

        val albumId = track?.album?.id
        if (albumId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response =
                        api.getAlbumDetails(albumId)  // Qui arriva l'oggetto Album aggiornato

                    withContext(Dispatchers.Main) {
                        val releaseDate = response.releaseDate ?: "N/A"
                        val genre = response.genres?.data?.firstOrNull()?.name ?: "N/A"
                        val date = formatDate(releaseDate)

                        findViewById<TextView>(R.id.date).text = "Data uscita: $date"
                        findViewById<TextView>(R.id.genre).text = "Genere: $genre"
                    }

                } catch (e: Exception) {
                    Log.e("Deezer", "Errore nel recupero dettagli album", e)
                }
            }
        }

        val songId = track?.id?.toString() ?: return
        val rating = findViewById<TextView>(R.id.rating)
        findViewById<TextView>(R.id.title).text = "${title ?: "N/A"}"
        findViewById<TextView>(R.id.artist).text = "${artist ?: "N/A"}"
        findViewById<TextView>(R.id.album).text = "${albumTitle ?: "N/A"}"
        val formattedDuration = formatDuration(duration)
        findViewById<TextView>(R.id.duration).text = "Durata: $formattedDuration"

        val coverImageView = findViewById<ImageView>(R.id.infoCover)
        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person) // immagine di default durante il caricamento
            .error(R.drawable.person)       // immagine se il caricamento fallisce
            .into(coverImageView)

        val db = FirebaseFirestore.getInstance()
        db.collection("Songs").document(songId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalSum = document.getDouble("totalRatingSum") ?: 0.0
                    val totalCount = document.getLong("totalRatings") ?: 0L

                    val averageRating = if (totalCount > 0) totalSum / totalCount else null

                    if (averageRating != null) {
                        rating.text = "%.1f".format(averageRating)
                    }
                }
            }
            .addOnFailureListener {
                rating.text = ""
                Log.e("Firestore", "Errore nel recupero rating", it)
            }

        val playButton = findViewById<Button>(R.id.play)

        playButton.setOnClickListener {
            if (!isPlaying && !preview.isNullOrEmpty()) {
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(preview)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setOnPreparedListener {
                            start()
                            this@TrackInformation.isPlaying = true
                            playButton.text = "⏸️ Stop"
                        }
                        setOnCompletionListener {
                            playButton.text = "▶️ Preview"
                            this@TrackInformation.isPlaying = false
                            release()
                            mediaPlayer = null
                        }
                        prepareAsync()
                    } catch (e: Exception) {
                        Log.e("MediaPlayer", "Errore durante la riproduzione: ${e.message}", e)
                        Toast.makeText(this@TrackInformation, "Errore audio", Toast.LENGTH_SHORT).show()
                    }
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
            addToPlaylist(track)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun formatDuration(seconds: Int?): String {
        if (seconds == null) return "Durata sconosciuta"
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d min", minutes, remainingSeconds)
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Data sconosciuta"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.ITALY)
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Data sconosciuta"
        }
    }

    fun addToPlaylist(track: Track?) {
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
                            this@TrackInformation,
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