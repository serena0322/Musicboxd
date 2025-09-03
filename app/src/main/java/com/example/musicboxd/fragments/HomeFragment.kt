package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        reviewAdapter = ReviewAdapter(
            reviewItems,
            onDeleteClick = { /* no-op nella Home */ },
            showAuthor = true
        )

        recyclerView.adapter = homeAdapter

        // Gradiente opzionale sul titolo
        view.findViewById<TextView>(R.id.Title)?.let { tv ->
            tv.post {
                val shader = LinearGradient(
                    0f, 0f, tv.width.toFloat(), 0f,
                    intArrayOf(Color.parseColor("#FF00AA"), Color.parseColor("#00CFFF")),
                    floatArrayOf(0.0f, 0.6f),
                    Shader.TileMode.CLAMP
                )
                tv.paint.shader = shader
                tv.invalidate()
            }
        }

        // Seleziona tab iniziale e aggiorna UI
        tabLayout.post {
            tabLayout.getTabAt(0)?.select()
            updateDisplayedTab(0)
        }

        // Avvio realtime per recensioni
        userViewModel.observeHomeReviewsRealtime()

        // Osserva le recensioni e ordinale per più recenti
        userViewModel.homeReviews.observe(viewLifecycleOwner) { list ->
            reviewItems.clear()
            reviewItems.addAll(
                list.sortedByDescending { it.orderKey() }
            )
            if (tabLayout.selectedTabPosition == 1) {
                reviewAdapter.notifyDataSetChanged()
            }
            if (list.isNotEmpty()) {
                userViewModel.resolveUsernamesFor(list.map { it.sourceUserId })
            }
        }

        // Aggiorna username autori quando la mappa cambia
        userViewModel.usernames.observe(viewLifecycleOwner) { map ->
            reviewAdapter.updateUsernames(map)
            if (tabLayout.selectedTabPosition == 1) {
                reviewAdapter.notifyDataSetChanged()
            }
        }

        setupTabListener()

        if (!tracksLoadedOnce || sections.isEmpty()) {
            loadTracksFromDeezer()
        }

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
            // TAB 0: Tracce
            0 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.home)
                )
                if (recyclerView.adapter !== homeAdapter) {
                    recyclerView.adapter = homeAdapter
                }
                if (!tracksLoadedOnce || sections.isEmpty()) {
                    loadTracksFromDeezer()
                }
            }
            // TAB 1: Recensioni
            1 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.search)
                )
                if (recyclerView.adapter !== reviewAdapter) {
                    recyclerView.adapter = reviewAdapter
                }
                if (reviewItems.isEmpty()) {
                    userViewModel.reloadHomeReviewsOnce()
                }
            }
            else -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.home)
                )
                if (recyclerView.adapter !== homeAdapter) {
                    recyclerView.adapter = homeAdapter
                }
                if (!tracksLoadedOnce || sections.isEmpty()) {
                    loadTracksFromDeezer()
                }
            }
        }
    }

    // --- Deezer (Tab 0) ---
    private fun loadTracksFromDeezer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Top Charts Globali (simile a Spotify Top 50)
                val globalChartsPage = RetrofitInstance.api.globalCharts()
                val globalTop = globalChartsPage.tracks?.data.orEmpty().take(20)

                // Altre categorie
                val pop = RetrofitInstance.api.searchTracks("pop").data.orEmpty().take(20)
                val rock = RetrofitInstance.api.searchTracks("rock").data.orEmpty().take(20)
                val hiphop = RetrofitInstance.api.searchTracks("hip hop").data.orEmpty().take(20)
                val indie = RetrofitInstance.api.searchTracks("indie").data.orEmpty().take(20)
                val electronic = RetrofitInstance.api.searchTracks("electronic").data.orEmpty().take(20)
                val italian = RetrofitInstance.api.searchTracks("italian").data.orEmpty().take(20)
                val fresh = RetrofitInstance.api.searchTracks("new").data.orEmpty().take(20)

                sections.clear()
                sections.add(TrackSection("Top Charts", globalTop)) // 👈 categoria simile a Spotify
                sections.add(TrackSection("Pop Hits", pop))
                sections.add(TrackSection("Rock Classics", rock))
                sections.add(TrackSection("Hip-Hop Vibes", hiphop))
                sections.add(TrackSection("Indie Discoveries", indie))
                sections.add(TrackSection("Electronic Essentials", electronic))
                sections.add(TrackSection("Italian Favorites", italian))
                sections.add(TrackSection("Fresh Finds", fresh))

                tracksLoadedOnce = true
                homeAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Errore caricamento tracce Deezer", e)
            }
        }
    }

    private data class CountrySpec(
        val title: String,
        val editorialId: Int
    )

    /** Specifica di una categoria (titolo mostrato + query Deezer + limite) */
    private data class CategorySpec(
        val title: String,
        val query: String,
        val limit: Int
    )

    private fun onTrackClick(track: Track) {
        // Adatta questi campi ai nomi reali nel tuo modello Track
        val coverUrl: String? = try {
            track.album?.cover
        } catch (_: Exception) { null }

        val intent = Intent(requireContext(), TrackInformation::class.java).apply {
            putExtra("track", track)     // Track deve essere Parcelable (vedi nota sotto)
            putExtra("cover", coverUrl)  // l’Activity legge "cover"
        }
        startActivity(intent)
    }
    private fun onTrackLongClick(track: Track) { /* TODO: azione long click */ }

    private fun Review.orderKey(): Long {
        return this.timestamp?.toDate()?.time ?: 0L
    }

}



