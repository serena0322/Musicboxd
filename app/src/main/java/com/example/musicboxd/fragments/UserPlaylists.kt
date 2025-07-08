package com.example.musicboxd.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicboxd.adapter.PlaylistAdapter
import com.example.musicboxd.local.PlaylistItem
import com.google.firebase.firestore.FirebaseFirestore

class UserPlaylists : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<PlaylistItem>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recupera l'userId passato come argomento
        userId = arguments?.getString("userId") ?: run {
            Log.e("UserPlaylists", "userId mancante")
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist, container, false)

        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val createButton = view.findViewById<TextView>(R.id.button)
        createButton.visibility = View.GONE

        // recupera l'userId dall'argomento SafeArgs
        val args: UserPlaylistsArgs by navArgs()
        userId = args.userId

        adapter = PlaylistAdapter(playlists,
            onItemClick = { playlistItem ->
                val action = UserPlaylistsDirections.actionUserplaylistToShowUserPlaylist(
                    playlistName = playlistItem.name,
                    userId = userId
                )
                findNavController().navigate(action)
            },
            onLongClick = {
            }
        )

        recyclerView.adapter = adapter

        loadUserPlaylists()

        return view
    }

    private fun loadUserPlaylists() {
        db.collection("User")
            .document(userId)
            .collection("Playlists")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("UserPlaylists", "Errore caricamento: ", error)
                    return@addSnapshotListener
                }

                playlists.clear()
                snapshots?.forEach { doc ->
                    val item = doc.toObject(PlaylistItem::class.java)
                    playlists.add(item)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
