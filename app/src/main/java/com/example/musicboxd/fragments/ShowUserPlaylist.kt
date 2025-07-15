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

class ShowUserPlaylist : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val trackList = mutableListOf<Track>()
    private lateinit var adapter: TrackAdapter
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.show_song_playlist, container, false)

        val args: ShowUserPlaylistArgs by navArgs()
        val playlistName = args.playlistName
        val userId = args.userId

        recyclerView = view.findViewById(R.id.RecyclerView)
        adapter = TrackAdapter(
            onItemClick = { track ->
                Log.d("TrackClick", "Hai cliccato su: ${track.title}")
            },
            onLongClick = {}
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<TextView>(R.id.name_playlist).text = playlistName

        // 🔍 Recupera la playlist dell’utente specificato
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
                val track = RetrofitInstance.api.getTrack(trackId)
                trackList.add(track)
                adapter.submitList(trackList.toList())
            } catch (e: Exception) {
                Log.e("API_ERROR", "Errore nel recupero traccia $trackId", e)
            }
        }
    }
}
