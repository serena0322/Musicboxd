package com.example.musicboxd.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.musicboxd.R
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : Fragment() {

    private var userId: String? = null
    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var followingCountTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTextView = view.findViewById(R.id.username)
        followersCountTextView = view.findViewById(R.id.followers)
        followingCountTextView = view.findViewById(R.id.following)

        userId = arguments?.getString("USER_ID")

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
