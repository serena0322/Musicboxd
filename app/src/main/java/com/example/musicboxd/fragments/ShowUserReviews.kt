package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.fragment.navArgs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ReviewAdapter
import com.example.musicboxd.local.Review
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import com.example.musicboxd.`object`.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ShowUserReviews : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()
    private val args: ShowUserReviewsArgs by navArgs()
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.show_reviews, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = ReviewAdapter(reviewList) { }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Caricamento recensioni asincrono
        viewLifecycleOwner.lifecycleScope.launch {
            val reviews = UserRepository.loadReviewsForUser(args.userId)
            reviewList.clear()
            reviewList.addAll(reviews)
            adapter.notifyDataSetChanged()
            recyclerView.visibility = View.VISIBLE
        }

        return view
    }


    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadReviews(userId: String): List<Review> {
        val result = db.collection("User")
            .document(userId)
            .collection("Reviews")
            .get()
            .await()

        return result.mapNotNull { doc ->
            val documentId = doc.id
            val songTitle = doc.getString("title") ?: return@mapNotNull null
            val artistName = doc.getString("artist") ?: return@mapNotNull null
            val timestamp = doc.getTimestamp("timestamp")
            val rating = doc.getDouble("rating") ?: 0.0
            val reviewText = doc.getString("textReview") ?: ""
            val cover = doc.getString("cover") ?: ""

            Review(
                documentId = documentId,
                actionType = "review",
                artistName = artistName,
                songTitle = songTitle,
                sourceUserId = userId,
                albumCoverUrl = cover,
                rating = rating,
                reviewText = reviewText,
                timestamp = timestamp
            )
        }
    }
}
