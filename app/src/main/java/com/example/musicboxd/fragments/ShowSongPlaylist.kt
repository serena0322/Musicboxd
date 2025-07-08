package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter
import com.example.musicboxd.network.RetrofitInstance
import com.example.musicboxd.network.Track
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ShowSongPlaylist : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val trackList = mutableListOf<Track>()  // usa il tuo modello completo
    private lateinit var adapter: TrackAdapter
    private val db = FirebaseFirestore.getInstance()

    private val args: ShowSongPlaylistArgs by navArgs()


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.show_song_playlist, container, false)
        val playlistId = args.playlistId
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return view

        val db = FirebaseFirestore.getInstance()
        val playlistRef = db.collection("User")
            .document(userId)
            .collection("Playlists")
            .document(playlistId)

        // 🔹 Recupera il nome e la lista dei brani
        playlistRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val playlistName = document.getString("name") ?: "Playlist"
                    view.findViewById<TextView>(R.id.NamePlaylist).text = playlistName

                    val trackIds = document.get("tracks") as? List<String>
                    if (!trackIds.isNullOrEmpty()) {
                        trackList.clear()
                        for (trackId in trackIds) {
                            fetchTrackById(trackId) // 🔁 tua funzione per caricare il brano
                        }
                    }
                } else {
                    view.findViewById<TextView>(R.id.NamePlaylist).text = "Playlist non trovata"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Errore nel recupero playlist", e)
                view.findViewById<TextView>(R.id.NamePlaylist).text = "Errore caricamento"
            }

        // 🔹 Setup RecyclerView
        recyclerView = view.findViewById(R.id.RecyclerView)
        adapter = TrackAdapter(
            onItemClick = { track ->
                // azione al click normale (puoi lasciarlo vuoto o gestirlo)
                Log.d("TrackClick", "Hai cliccato su: ${track.title}")
            },
            onLongClick = { track ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Rimuovi brano")
                    .setMessage("Vuoi rimuovere \"${track.title}\" dalla playlist?")
                    .setPositiveButton("Rimuovi") { _, _ ->
                        removeTrackFromPlaylist(track)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    private fun removeTrackFromPlaylist(track: Track) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val playlistId = args.playlistId

        val playlistRef = FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .collection("Playlists")
            .document(playlistId)

        playlistRef.update("tracks", FieldValue.arrayRemove(track.id))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Brano rimosso", Toast.LENGTH_SHORT).show()

                val index = trackList.indexOfFirst { it.id == track.id }
                if (index != -1) {
                    trackList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nella rimozione", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTrackById(trackId: String) {
        lifecycleScope.launch {
            try {
                val track = RetrofitInstance.api.getTrack(trackId)
                trackList.add(track)
                adapter.submitList(trackList.toList()) // forza il ricalcolo della differenza
            } catch (e: Exception) {
                Log.e("API_ERROR", "Errore nel recupero traccia $trackId", e)
            }
        }
    }

}
