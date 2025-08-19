package com.example.musicboxd.fragments

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.HomeAdapter
import com.example.musicboxd.adapter.TrackSection
import com.example.musicboxd.network.Track
import com.example.musicboxd.network.RetrofitInstance
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    // Ora gestiamo più sezioni, non solo i trend
    private val sections = mutableListOf<TrackSection>()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(sections, ::onTrackClick, ::onTrackLongClick)
        recyclerView.adapter = homeAdapter

        setupTabs()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.Title)
        textView?.post {
            val textWidth = textView.width.toFloat()
            val startColor = Color.parseColor("#FF00AA")
            val endColor = Color.parseColor("#00CFFF")
            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,
                intArrayOf(startColor, endColor),
                floatArrayOf(0.0f, 0.6f),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
        }

        loadTracksFromDeezer()
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val color = when (tab.position) {
                    0 -> R.color.home
                    1 -> R.color.search
                    2 -> R.color.notify
                    3 -> R.color.light_heavenly
                    else -> R.color.home
                }
                tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), color))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadTracksFromDeezer() {
        lifecycleScope.launch {
            try {
                // Puoi fare più chiamate API per categorie diverse
                val trendingResponse = RetrofitInstance.api.searchTracks("trending")
                val popResponse = RetrofitInstance.api.searchTracks("pop")
                val rockResponse = RetrofitInstance.api.searchTracks("rock")

                sections.clear()
                sections.add(TrackSection("Trending Tracks", trendingResponse.data))
                sections.add(TrackSection("Pop Hits", popResponse.data))
                sections.add(TrackSection("Rock Classics", rockResponse.data))

                homeAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Errore caricamento tracce Deezer", e)
            }
        }
    }

    private fun onTrackClick(track: Track) {
        // TODO: gestisci click sul brano
    }

    private fun onTrackLongClick(track: Track) {
        // TODO: gestisci long click sul brano
    }
}