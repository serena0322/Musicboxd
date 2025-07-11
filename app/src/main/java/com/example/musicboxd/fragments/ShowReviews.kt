package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ReviewAdapter
import com.example.musicboxd.local.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.musicboxd.viewModels.UserViewModel
import kotlin.getValue


class ShowReviews : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.show_reviews, container, false)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerView)

        adapter = ReviewAdapter(reviewList) { review ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminare recensione")
                .setMessage("Sei sicura di voler eliminare questa recensione?")
                .setPositiveButton("Elimina") { _, _ ->
                    deleteReview(review.sourceUserId, review.documentId)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        userViewModel.basicProfile.observe(viewLifecycleOwner) { profile ->
            val reviews = profile.reviews
            reviewList.clear()
            reviewList.addAll(reviews.sortedByDescending { it.timestamp })
            adapter.notifyDataSetChanged()

        }

        return view
    }

    private fun deleteReview(userId: String, documentId: String) {
        db.collection("User")
            .document(userId)
            .collection("Reviews")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                val index = reviewList.indexOfFirst { it.documentId == documentId }
                if (index != -1) {
                    reviewList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
                Toast.makeText(context, "Recensione eliminata", Toast.LENGTH_SHORT).show()

                // 🔁 Aggiorna il profilo per ricaricare le review aggiornate
                userViewModel.loadMyBasicProfile(forceReload = true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Errore nell'eliminazione", Toast.LENGTH_SHORT).show()
            }
    }
}
