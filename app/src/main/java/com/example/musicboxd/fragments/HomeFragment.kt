package com.example.musicboxd.fragments
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.SongAdapter
import com.example.musicboxd.classes.Image
import com.example.musicboxd.local.Album
import com.example.musicboxd.local.Artist
import com.example.musicboxd.local.Song
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter

    // Liste dati
    private val songs = mutableListOf<Song>()
    private val artistMap = mutableMapOf<String, Artist>()
    private val albumMap = mutableMapOf<String, Album>()
    private val albumImagesMap = mutableMapOf<String, List<Image>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflazione del layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SongAdapter(songs, artistMap, albumMap, albumImagesMap)
        recyclerView.adapter = adapter

        loadSongsFromFirestore()
        // Listener dei tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.home)
                        )
                    }

                    1 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.add)
                        )
                    }

                    2 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.teal_200)
                        )
                    }

                    3 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.profile)
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        return view
    }

    private fun loadSongsFromFirestore() {
        val db = Firebase.firestore

        db.collection("Song").get().addOnSuccessListener { songSnapshot ->
            val songList = songSnapshot.toObjects(Song::class.java)
            songs.clear()
            songs.addAll(songList)

            db.collection("Artist").get().addOnSuccessListener { artistSnapshot ->
                val newArtistMap: Map<String, Artist> = artistSnapshot.documents.mapNotNull { doc ->
                    val artist = doc.toObject(Artist::class.java)
                    val id = doc.id
                    if (artist != null) id to artist else null
                }.toMap()

                this.artistMap.clear()
                this.artistMap.putAll(newArtistMap)
                adapter.updateArtistMap(this.artistMap)

                db.collection("Album").get().addOnSuccessListener { albumSnapshot ->
                    val newAlbumMap: Map<String, Album> = albumSnapshot.documents.mapNotNull { doc ->
                        val album = doc.toObject(Album::class.java)
                        val id = doc.id
                        if (album != null) id to album else null
                    }.toMap()

                    this.albumMap.clear()
                    this.albumMap.putAll(newAlbumMap)
                    adapter.updateAlbumMap(this.albumMap)

                    db.collection("Image").get().addOnSuccessListener { imageSnapshot ->
                        val imageList = imageSnapshot.toObjects(Image::class.java)

                        this.albumImagesMap.clear()
                        this.albumImagesMap.putAll(
                            imageList.filter { it.albumId.isNotBlank() }
                                .groupBy { it.albumId }
                        )

                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Errore nel caricamento songs: ${it.message}", it)
            Toast.makeText(
                requireContext(),
                "Errore nel caricamento dati: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }



    //sfumatura titolo
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializzazione del TextView dopo l'inflazione e controllo su null
        val textView = view.findViewById<TextView>(R.id.Title)

        // Utilizzo di post() per eseguire codice sulla UI dopo che la vista è pronta
        textView?.post {
            // Misura la larghezza del testo
//            val textWidth = textView.paint.measureText(textView.text.toString())
            val textWidth = textView.width.toFloat()

            // Colori per la sfumatura
            val startColor = ContextCompat.getColor(requireContext(), R.color.home)
            val endColor = ContextCompat.getColor(requireContext(), R.color.teal_200)

            // Creazione della sfumatura orizzontale
            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,  // Sfumatura orizzontale (da sinistra a destra)
                intArrayOf(startColor, endColor),
                floatArrayOf(0.0f, 0.6f), // Colori distribuiti in modo uniforme da 0% a 100%
                Shader.TileMode.CLAMP
            )

            // Applicazione della sfumatura al TextView
            textView.paint.shader = shader
            textView.invalidate()
        }

    }
}
