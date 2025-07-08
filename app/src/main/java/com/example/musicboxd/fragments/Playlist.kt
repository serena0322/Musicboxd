package com.example.musicboxd.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.PlaylistAdapter
import com.example.musicboxd.local.PlaylistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Playlist : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<PlaylistItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist, container, false)

        auth = FirebaseAuth.getInstance()

        // 1. Inizializza prima l'adapter con il listener
        adapter = PlaylistAdapter(playlists) { playlistItem ->
            findNavController().navigate(R.id.action_playlist_to_showSongPlaylist)
        }

        // 2. Poi assegnalo al RecyclerView
        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // 3. Assegna listener al pulsante
        view.findViewById<TextView>(R.id.button).setOnClickListener {
            createPlaylist()
        }

        // 4. Carica i dati (verrà chiamato dopo aver impostato l’adapter)
        loadPlaylists()

        return view
    }

    private fun loadPlaylists() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("User")
            .document(userId)
            .collection("Playlists")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener
                playlists.clear()
                for (doc in snapshots) {
                    val item = doc.toObject(PlaylistItem::class.java)
                    playlists.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun createPlaylist() {
        val input = EditText(requireContext()).apply {
            hint = "Inserisci il nome della playlist"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuova Playlist")
            .setView(input)
            .setPositiveButton("Crea") { dialog, _ ->
                val playlistName = input.text.toString().trim()
                if (playlistName.isEmpty()) {
                    Toast.makeText(requireContext(), "Nome non valido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Toast.makeText(requireContext(), "Utente non autenticato", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val playlistData = hashMapOf(
                    "name" to playlistName,
                    "createdBy" to userId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "tracks" to emptyList<String>() // Campo opzionale per inizializzazione
                )

                db.collection("User")
                    .document(userId)
                    .collection("Playlists")
                    .add(playlistData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Playlist creata", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Playlist", "Errore nella creazione", e)
                        Toast.makeText(requireContext(), "Errore nella creazione", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

}