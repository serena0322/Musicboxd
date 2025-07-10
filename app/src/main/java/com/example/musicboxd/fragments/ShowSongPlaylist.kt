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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter
import com.example.musicboxd.network.RetrofitInstance
import com.example.musicboxd.network.Track
import com.example.musicboxd.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlin.getValue

class ShowSongPlaylist : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val args: ShowSongPlaylistArgs by navArgs()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var nameTextView: TextView
    private val trackList = mutableListOf<Track>()

    private var playlistListener: ListenerRegistration? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.show_song_playlist, container, false)

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Forza il refresh del profilo in caso sia obsoleto (specie dopo modifiche)
        userViewModel.loadMyBasicProfile(forceReload = true)

        nameTextView = view.findViewById(R.id.NamePlaylist)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.RecyclerView)
        adapter = TrackAdapter(
            onItemClick = { track ->
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

        observePlaylistChanges()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playlistListener?.remove()
    }

    private fun loadTracksFromIds(trackIds: List<String>) {
        lifecycleScope.launch {
            trackList.clear() // Pulizia preventiva
            val fetchedTracks = mutableListOf<Track>()

            for (id in trackIds) {
                try {
                    val track = RetrofitInstance.api.getTrack(id)
                    fetchedTracks.add(track)
                } catch (e: Exception) {
                    Log.e("API", "Errore caricamento traccia $id", e)
                }
            }

            trackList.addAll(fetchedTracks)
            adapter.submitList(trackList.toList())  // Nuova lista, per sicurezza
        }
    }

    private fun observePlaylistChanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val playlistId = args.playlistId

        playlistListener?.remove() // rimuove un eventuale listener precedente

        val playlistRef = FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .collection("Playlists")
            .document(playlistId)

        playlistListener = playlistRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            val name = snapshot.getString("name") ?: "Playlist"
            nameTextView.text = name

            val trackIds = snapshot.get("tracks") as? List<String> ?: emptyList()
            loadTracksFromIds(trackIds)
        }
    }

    private fun removeTrackFromPlaylist(track: Track) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val playlistId = args.playlistId

        val playlistRef = FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .collection("Playlists")
            .document(playlistId)

        val trackIdString = track.id.toString() // conversione

        playlistRef.update("tracks", FieldValue.arrayRemove(trackIdString))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Brano rimosso", Toast.LENGTH_SHORT).show()

                // Aggiorna localmente
                trackList.removeAll { it.id.toString() == trackIdString }
                adapter.submitList(trackList.toList())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nella rimozione", Toast.LENGTH_SHORT).show()
            }
    }

}
