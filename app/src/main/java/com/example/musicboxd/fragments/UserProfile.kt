package com.example.musicboxd.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicboxd.R
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : Fragment() {

    private lateinit var userId: String
    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var followingCountTextView: TextView

    private val args: UserProfileArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userId = args.userId

        return inflater.inflate(R.layout.user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTextView = view.findViewById(R.id.username)
        followersCountTextView = view.findViewById(R.id.followers)
        followingCountTextView = view.findViewById(R.id.following)
        val playlist = view.findViewById<TextView>(R.id.playlist)

        playlist.setOnClickListener {
            val action = UserProfileDirections.actionUserProfileToUserplaylist(userId)
            findNavController().navigate(action)
        }

        val reviews = view.findViewById<TextView>(R.id.reviews)

        reviews.setOnClickListener {
            val action = UserProfileDirections.actionUserProfileToShowUserReviews(userId)
            findNavController().navigate(action)
        }

        userId?.let { userId ->
            loadUserData(userId)
            loadFollowersCount(userId)
            loadFollowingCount(userId)
        }
    }

    private fun loadFollowersCount(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val followersCount = document.getLong("followers")?.toInt() ?: 0
                    followersCountTextView.text = "Followers: $followersCount"
                } else {
                    Log.w("UserProfile", "Documento non trovato per ID: $userId")
                    followersCountTextView.text = "Followers: 0"
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserProfile", "Errore nel recupero del campo followers", e)
                followersCountTextView.text = "Followers: 0"
            }
    }


    private fun loadFollowingCount(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val followingCount = document.getLong("following")?.toInt() ?: 0
                    followingCountTextView.text = "Following: $followingCount"
                } else {
                    Log.w("UserProfile", "Documento non trovato per ID: $userId")
                    followingCountTextView.text = "Following: 0"
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserProfile", "Errore nel recupero del campo following", e)
                followingCountTextView.text = "Following: 0"
            }
    }


    private fun loadUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    usernameTextView.text = username
                } else {
                    Log.w("UserProfile", "Documento non trovato per ID: $userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserProfile", "Errore nel recupero dati utente", e)
            }
    }
}
