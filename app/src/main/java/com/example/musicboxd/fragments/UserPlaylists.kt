package com.example.musicboxd.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.PlaylistAdapter
import com.example.musicboxd.local.PlaylistItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserPlaylists : Fragment() {

    private var playlistsRegistration: ListenerRegistration? = null

    private val args: UserPlaylistsArgs by navArgs()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<PlaylistItem>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = args.userId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist, container, false)

        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Nasconde il pulsante di creazione (schermata "UserPlaylists" è solo lettura)
        view.findViewById<TextView>(R.id.button).visibility = View.GONE

        adapter = PlaylistAdapter(
            playlists = playlists,
            onItemClick = { playlistItem ->
                val bundle = Bundle().apply {
                    putString("playlistId", playlistItem.id)
                    putString("userId", userId)
                }
                findNavController().navigate(
                    R.id.action_userplaylist_to_showUserPlaylist,
                    bundle
                )
            },
            onLongClick = { /* opzionale: mostrare menu contestuale */ }
        )

        recyclerView.adapter = adapter

        loadUserPlaylists()

        return view
    }

    override fun onDestroyView() {
        playlistsRegistration?.remove()
        playlistsRegistration = null
        super.onDestroyView()
    }

    private fun loadUserPlaylists() {
        playlistsRegistration = db.collection("User")
            .document(userId)
            .collection("Playlists")
            .addSnapshotListener(requireActivity()) { snapshots, error ->
                if (error != null) {
                    Log.e("UserPlaylists", "Errore caricamento: ", error)
                    return@addSnapshotListener
                }

                playlists.clear()
                snapshots?.forEach { doc ->
                    // doc è QueryDocumentSnapshot
                    val item = doc.toObject(PlaylistItem::class.java).copy(id = doc.id)
                    playlists.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
