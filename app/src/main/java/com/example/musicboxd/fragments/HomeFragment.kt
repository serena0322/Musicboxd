package com.example.musicboxd.fragments
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter
import com.example.musicboxd.local.Album
import com.example.musicboxd.local.Artist
import com.example.musicboxd.local.Image
import com.google.android.material.tabs.TabLayout


class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    // Liste dati
//    private val songs = mutableListOf<Song>()
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
                            ContextCompat.getColor(requireContext(), R.color.search)
                        )
                    }

                    2 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.notify)
                        )
                    }

                    3 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.light_heavenly)
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

                // Sfumatura fucsia → azzurro
                val startColor = Color.parseColor("#FF00AA") // Fucsia
                val endColor = Color.parseColor("#00CFFF")   // Azzurro

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
