package com.example.musicboxd.fragments

import android.annotation.SuppressLint
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.HomeAdapter
import com.example.musicboxd.adapter.ReviewAdapter
import com.example.musicboxd.adapter.TrackSection
import com.example.musicboxd.local.Review
import com.example.musicboxd.network.RetrofitInstance
import com.example.musicboxd.network.Track
import com.example.musicboxd.viewModels.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    private val sections = mutableListOf<TrackSection>()
    private lateinit var homeAdapter: HomeAdapter

    private val reviewItems = mutableListOf<Review>()
    private lateinit var reviewAdapter: ReviewAdapter

    private var tracksLoadedOnce = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        homeAdapter = HomeAdapter(sections, ::onTrackClick, ::onTrackLongClick)
        reviewAdapter = ReviewAdapter(reviewItems) { /* long press opzionale */ }

        recyclerView.adapter = homeAdapter


        // Se vuoi vedere un gradiente sul titolo, opzionale
        val titleView = view.findViewById<TextView>(R.id.Title)
        titleView?.let { tv ->
            tv.post {
                val textWidth = tv.width.toFloat()
                val shader = LinearGradient(
                    0f, 0f, textWidth, 0f,
                    intArrayOf(Color.parseColor("#FF00AA"), Color.parseColor("#00CFFF")),
                    floatArrayOf(0.0f, 0.6f),
                    Shader.TileMode.CLAMP
                )
                tv.paint.shader = shader
                tv.invalidate()
            }
        }

        // Tab iniziale
        tabLayout.post {
            tabLayout.getTabAt(0)?.select()
            updateDisplayedTab(0)
        }

        // Avvia realtime e osserva UNA sola volta
        userViewModel.observeHomeReviewsRealtime()
        userViewModel.homeReviews.observe(viewLifecycleOwner) { reviews ->
            if (tabLayout.selectedTabPosition == 1) {
                reviewItems.clear()
                reviewItems.addAll(reviews)
                reviewAdapter.notifyDataSetChanged()
            }
        }

        setupTabListener()

        if (!tracksLoadedOnce || sections.isEmpty()) loadTracksFromDeezer()

        return view
    }

    private fun setupTabListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = updateDisplayedTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) = updateDisplayedTab(tab.position)
        })
    }

    private fun updateDisplayedTab(position: Int) {
        when (position) {
            0 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.home)
                )
                if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
                if (!tracksLoadedOnce || sections.isEmpty()) loadTracksFromDeezer()
            }
            1 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.search)
                )
                if (recyclerView.adapter !== reviewAdapter) recyclerView.adapter = reviewAdapter
                if (reviewItems.isEmpty()) userViewModel.reloadHomeReviewsOnce()
            }
            else -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.home)
                )
                if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
                if (!tracksLoadedOnce || sections.isEmpty()) loadTracksFromDeezer()
            }
        }
    }

    // --- Deezer (Tab 0) ---
    private fun loadTracksFromDeezer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val trending = RetrofitInstance.api.searchTracks("trending").data
                val pop = RetrofitInstance.api.searchTracks("pop").data
                val rock = RetrofitInstance.api.searchTracks("rock").data

                sections.clear()
                sections.add(TrackSection("Trending Tracks", trending))
                sections.add(TrackSection("Pop Hits", pop))
                sections.add(TrackSection("Rock Classics", rock))

                tracksLoadedOnce = true
                homeAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Errore caricamento tracce Deezer", e)
            }
        }
    }

    // --- DEBUG opzionale: probe su collectionGroup, rimuovere in produzione ---
    override fun onResume() {
        super.onResume()
        probeCollectionGroupReviews()
    }

    private fun probeCollectionGroupReviews() {
        val db = FirebaseFirestore.getInstance()
        Log.d("CGQ", "Query: collectionGroup('Reviews')")
        db.collectionGroup("Reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { qs ->
                Log.d("CGQ", "size=${qs.size()}")
                qs.documents.forEach { d ->
                    val author = d.reference.parent.parent?.id ?: "(unknown)"
                    Log.d("CGQ", "path=${d.reference.path} | author=$author | data=${d.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CGQ", "collectionGroup FAILED", e)
            }
    }

    private fun onTrackClick(track: Track) { /* TODO */ }
    private fun onTrackLongClick(track: Track) { /* TODO */ }
}