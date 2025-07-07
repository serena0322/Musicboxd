package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ShowSongPlaylist : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val trackList = mutableListOf<Track>()  // usa il tuo modello completo
    private lateinit var adapter: TrackAdapter
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.show_song_playlist, container, false)

        val args: ShowSongPlaylistArgs by navArgs()
        val playlistName = args.playlistName
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return view

        recyclerView = view.findViewById(R.id.RecyclerView)
        adapter = TrackAdapter { track ->
            // Azione al click (puoi lasciare vuoto o gestirlo)
            Log.d("TrackClick", "Hai cliccato su: ${track.title}")
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<TextView>(R.id.NamePlaylist).text = playlistName

        // 🔍 Recupera i track ID da Firestore
        db.collection("User")
            .document(userId)
            .collection("Playlists")
            .whereEqualTo("name", playlistName)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val playlistDoc = result.documents[0]
                    val trackIds = playlistDoc.get("tracks") as? List<String>

                    if (!trackIds.isNullOrEmpty()) {
                        trackList.clear()

                        for (trackId in trackIds) {
                            // 🔁 Qui chiama la tua API per ottenere il brano completo
                            fetchTrackById(trackId)
                        }
                    }
                }
            }

        return view
    }

    private fun fetchTrackById(trackId: String) {
        lifecycleScope.launch {
            try {
                val track = RetrofitInstance.api.getTrackById(trackId)
                trackList.add(track)
                adapter.submitList(trackList.toList()) // aggiornamento della lista se usi ListAdapter
            } catch (e: Exception) {
                Log.e("API_ERROR", "Errore nel recupero traccia $trackId", e)
            }
        }
    }

}
