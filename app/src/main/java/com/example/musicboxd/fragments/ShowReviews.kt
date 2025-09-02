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
import com.example.musicboxd.viewModels.UserViewModel
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import java.util.Locale
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

        adapter = ReviewAdapter(
            reviews = reviewList,
            onDeleteClick = { review ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminare recensione")
                    .setMessage("Sei sicura di voler eliminare questa recensione?")
                    .setPositiveButton("Elimina") { _, _ ->
                        deleteReview(review.sourceUserId, review.documentId)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            },
            showAuthor = false // qui non mostrare l’autore
        )

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
        val reviewRef = db.collection("User")
            .document(userId)
            .collection("Reviews")
            .document(documentId)

        reviewRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val rating = snapshot.getDouble("rating") ?: return@addOnSuccessListener
                val songTitle = snapshot.getString("title") ?: return@addOnSuccessListener
                val artistName = snapshot.getString("artist") ?: return@addOnSuccessListener

                // Trova il documento della canzone tramite title + artist (assumendo songId è basato su questo)
                db.collection("Songs")
                    .whereEqualTo("title", songTitle)
                    .whereEqualTo("artist", artistName)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { songDocs ->
                        if (!songDocs.isEmpty) {
                            val songDoc = songDocs.first()
                            val songRef = songDoc.reference
                            val ratingKey = String.format(Locale.US, "%.1f", rating)

                            db.runTransaction { transaction ->
                                val songSnapshot = transaction.get(songRef)
                                val currentSum = songSnapshot.getDouble("totalRatingSum") ?: 0.0
                                val currentCount = songSnapshot.getLong("totalRatings") ?: 0L
                                val histogramPath = FieldPath.of("ratingsHistogram", ratingKey)

                                // Aggiorna i valori
                                transaction.update(songRef, "totalRatingSum", currentSum - rating)
                                transaction.update(songRef, "totalRatings", currentCount - 1)
                                transaction.update(songRef, histogramPath, FieldValue.increment(-1))
                            }
                        }

                        // Solo dopo aggiorna la recensione
                        reviewRef.delete()
                            .addOnSuccessListener {
                                val index = reviewList.indexOfFirst { it.documentId == documentId }
                                if (index != -1) {
                                    reviewList.removeAt(index)
                                    adapter.notifyItemRemoved(index)
                                }
                                Toast.makeText(context, "Recensione eliminata", Toast.LENGTH_SHORT).show()
                                userViewModel.loadMyBasicProfile(forceReload = true)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Errore nell'eliminazione", Toast.LENGTH_SHORT).show()
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Errore nel recupero della canzone", Toast.LENGTH_SHORT).show()
                    }

            }
        }.addOnFailureListener {
            Toast.makeText(context, "Errore durante l'accesso alla recensione", Toast.LENGTH_SHORT).show()
        }
    }

}
