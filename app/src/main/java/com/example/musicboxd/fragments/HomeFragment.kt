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
import com.example.musicboxd.reviews.ReviewFeedItem
import com.example.musicboxd.reviews.ReviewsFeedAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Collections

class HomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    // Adapter tracce (Deezer)
    private val sections = mutableListOf<TrackSection>()
    private lateinit var homeAdapter: HomeAdapter

    // Listener dei tab (per evitare duplicazioni)
    private var tabListener: TabLayout.OnTabSelectedListener? = null

    // Adapter recensioni (Firestore)
    private lateinit var reviewsAdapter: ReviewsFeedAdapter
    private val reviews = mutableListOf<ReviewFeedItem>()

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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
        reviewsAdapter = ReviewsFeedAdapter(reviews, ::onReviewClick, ::onReviewLongClick)

        // Di default mostriamo la Home con le tracce (Tab 0)
        recyclerView.adapter = homeAdapter

        setupTabs()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Effetto gradiente sul titolo (opzionale)
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

        // Selezione e caricamento iniziali "safe" del Tab 0
        tabLayout.post {
            if (tabLayout.tabCount > 0) {
                val tab0 = tabLayout.getTabAt(0)
                if (tabLayout.selectedTabPosition != 0) {
                    tab0?.select()
                }
                // Fallback: assicura contenuto immediato anche se l'evento non scatta
                if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
                setIndicatorColor(R.color.home)
                if (sections.isEmpty()) loadTracksFromDeezer()
            }
        }
    }

    /** Richiamabile dalla Activity per forzare la Home (Tab 0) su reselect del bottom-nav */
    fun selectDefaultTab0() {
        tabLayout.post {
            tabLayout.getTabAt(0)?.select()
            if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
            setIndicatorColor(R.color.home)
            if (sections.isEmpty()) loadTracksFromDeezer()
        }
    }

    private fun setupTabs() {
        tabListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    1 -> {
                        setIndicatorColor(R.color.search)
                        if (recyclerView.adapter !== reviewsAdapter) recyclerView.adapter = reviewsAdapter
                        if (reviews.isEmpty()) loadRandomReviews()
                    }
                    else -> {
                        val color = when (tab.position) {
                            0 -> R.color.home
                            2 -> R.color.notify
                            3 -> R.color.light_heavenly
                            else -> R.color.home
                        }
                        setIndicatorColor(color)
                        if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
                        if (sections.isEmpty()) loadTracksFromDeezer()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            // Gestione del reselect: utile quando il Tab 0 è già selezionato e si vuole forzare il refresh
            override fun onTabReselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        if (recyclerView.adapter !== homeAdapter) recyclerView.adapter = homeAdapter
                        if (sections.isEmpty()) loadTracksFromDeezer()
                    }
                    1 -> {
                        if (recyclerView.adapter !== reviewsAdapter) recyclerView.adapter = reviewsAdapter
                        if (reviews.isEmpty()) loadRandomReviews()
                    }
                }
            }
        }
        tabLayout.addOnTabSelectedListener(tabListener!!)
    }

    override fun onDestroyView() {
        tabListener?.let { tabLayout.removeOnTabSelectedListener(it) }
        tabListener = null
        recyclerView.adapter = null
        super.onDestroyView()
    }

    private fun setIndicatorColor(colorRes: Int) {
        tabLayout.setSelectedTabIndicatorColor(
            ContextCompat.getColor(requireContext(), colorRes)
        )
    }

    /** Caricamento sezioni Deezer (Tab 0 e altri non-1) */
    private fun loadTracksFromDeezer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
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

    /** Caricamento recensioni casuali (mix seguiti e non) per Tab 1 */
    private fun loadRandomReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val uid = auth.currentUser?.uid
                // 1) Lista seguiti
                val following: Set<String> = if (uid != null) {
                    val userDoc = firestore.collection("Users").document(uid).get().await()
                    @Suppress("UNCHECKED_CAST")
                    (userDoc.get("following") as? List<String>)?.toSet() ?: emptySet()
                } else emptySet()

                // 2) Pool di recensioni recenti
                val snap = firestore.collection("Reviews")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()

                val pool = snap.documents.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    ReviewFeedItem(
                        reviewId = doc.id,
                        userId = userId,
                        username = doc.getString("username") ?: userId,
                        songTitle = doc.getString("songTitle") ?: "",
                        artistName = doc.getString("artistName") ?: "",
                        rating = doc.getDouble("rating"),
                        reviewText = doc.getString("reviewText"),
                        coverUrl = doc.getString("coverUrl"),
                        timestamp = doc.getTimestamp("timestamp")?.toDate()
                    )
                }

                // 3) Split seguiti / altri
                val (fromFollowed, fromOthers) = pool.partition { following.contains(it.userId) }

                // 4) Campionamento e shuffle
                val takeFollowed = fromFollowed.take(10)
                val shuffledOthers = fromOthers.toMutableList().also { Collections.shuffle(it) }
                val finalList = (takeFollowed + shuffledOthers.take(20)).toMutableList()
                Collections.shuffle(finalList)

                reviews.clear()
                reviews.addAll(finalList)
                reviewsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Errore caricamento recensioni", e)
            }
        }
    }

    private fun onTrackClick(track: Track) {
        // TODO: gestisci click sul brano
    }

    private fun onTrackLongClick(track: Track) {
        // TODO: gestisci long click sul brano
    }

    private fun onReviewClick(item: ReviewFeedItem) {
        // TODO: apri dettaglio recensione / brano / profilo
    }

    private fun onReviewLongClick(item: ReviewFeedItem) {
        // TODO: azioni (segnala, condividi, ecc.)
    }
}
