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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflazione del layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Tab Music cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.home
                            )
                        )
                    }

                    1 -> {
                        // Tab Reviews cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.add
                            )
                        )
                    }

                    2 -> {
                        // Tab Lists cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.teal_200
                            )
                        )
                    }

                    3 -> {
                        // Tab Journal cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.profile
                            )
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Opzionale: azioni quando il tab viene deselezionato
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Opzionale: azioni quando il tab viene riselezionato
            }
        })

        val recyclerView = view.findViewById<RecyclerView>(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Esempio lista statica
        val musicList = listOf(
            MusicItem("Bohemian Rhapsody", "Queen"),
            MusicItem("Billie Jean", "Michael Jackson"),
            MusicItem("Imagine", "John Lennon"),
            MusicItem("Smells Like Teen Spirit", "Nirvana"),
            MusicItem("Like a Rolling Stone", "Bob Dylan"),
            MusicItem("Hotel California", "Eagles"),
            MusicItem("Hey Jude", "The Beatles"),
            MusicItem("Lose Yourself", "Eminem"),
            MusicItem("Hallelujah", "Leonard Cohen"),
            MusicItem("Wonderwall", "Oasis"),
            MusicItem("Rolling in the Deep", "Adele"),
            MusicItem("Shape of You", "Ed Sheeran"),
            MusicItem("Blinding Lights", "The Weeknd"),
            MusicItem("Bad Guy", "Billie Eilish"),
            MusicItem("Uptown Funk", "Mark Ronson ft. Bruno Mars"),
        )

        val adapter = MusicAdapter(musicList)
        recyclerView.adapter = adapter

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializzazione del TextView dopo l'inflazione e controllo su null
        val textView = view.findViewById<TextView>(R.id.Title)

        // Utilizzo di post() per eseguire codice sulla UI dopo che la vista è pronta
        textView?.post {
            // Misura la larghezza del testo
            val textWidth = textView.paint.measureText(textView.text.toString())

            // Colori per la sfumatura
            val startColor = ContextCompat.getColor(requireContext(), R.color.home)
            val endColor = ContextCompat.getColor(requireContext(), R.color.teal_200)

            // Creazione della sfumatura orizzontale
            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,  // Sfumatura orizzontale (da sinistra a destra)
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )

            // Applicazione della sfumatura al TextView
            textView.paint.shader = shader
            textView.invalidate()
        }
    }
}