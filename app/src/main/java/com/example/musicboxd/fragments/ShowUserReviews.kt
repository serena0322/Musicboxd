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
import android.util.Log
import android.widget.Toast


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
        adapter = ReviewAdapter(reviewList) { /* Nessuna azione */ }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadReviews(args.userId)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadReviews(userId: String) {
        db.collection("User")
            .document(userId)
            .collection("Reviews")
            .get()
            .addOnSuccessListener { result ->
                reviewList.clear()
                recyclerView.visibility = View.VISIBLE

                for (doc in result) {
                    val documentId = doc.id
                    val songTitle = doc.getString("title") ?: continue
                    val artistName = doc.getString("artist") ?: continue
                    val timestamp = doc.getTimestamp("timestamp")
                    val rating = doc.getDouble("rating") ?: 0.0
                    val reviewText = doc.getString("textReview") ?: ""
                    val cover = doc.getString("cover") ?: ""

                    val review = Review(
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

                    reviewList.add(review)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Errore nel caricamento recensioni", it)
                Toast.makeText(requireContext(), "Errore nel caricamento", Toast.LENGTH_SHORT).show()
            }
    }

}
