package com.example.musicboxd

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.adapter.MusicAdapter
import com.google.android.material.tabs.TabLayout



class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MusicAdapter

    // Dati fittizi per ogni tab
    private val musicList = listOf(
        MusicItem("Bohemian Rhapsody", "Queen"),
        MusicItem("Billie Jean", "Michael Jackson"),
        MusicItem("Imagine", "John Lennon")
    )

    private val reviewList = listOf(
        MusicItem("Recensione 1", "Album A"),
        MusicItem("Recensione 2", "Album B"),
        MusicItem("Recensione 3", "Album C")
    )

    private val listList = listOf(
        MusicItem("Top 10 Rock", "Vari artisti"),
        MusicItem("Best of 2024", "Vari artisti"),
        MusicItem("Favorites", "Vari artisti")
    )

    private val journalList = listOf(
        MusicItem("Giorno 1", "Ascoltato Album X"),
        MusicItem("Giorno 2", "Scoperto Artista Y"),
        MusicItem("Giorno 3", "Playlist Z")
    )


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

        // Adapter inizializzato con la prima lista (Music)
        adapter = MusicAdapter(musicList)
        recyclerView.adapter = adapter

        // Listener dei tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        adapter.updateData(musicList)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.home)
                        )
                    }
                    1 -> {
                        adapter.updateData(reviewList)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.add)
                        )
                    }
                    2 -> {
                        adapter.updateData(listList)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.teal_200)
                        )
                    }
                    3 -> {
                        adapter.updateData(journalList)
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