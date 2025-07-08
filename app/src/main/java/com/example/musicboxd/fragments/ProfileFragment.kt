package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ProfileAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment: Fragment() {

    @SuppressLint("ServiceCast", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflazione del layout
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Trova il TextView dal layout
        val username: TextView = view.findViewById(R.id.Title)
        // Recupera l'username da SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("saved_username", "Guest")  // "Guest" è il valore predefinito nel caso non sia stato salvato nessun username
        // Imposta l'username nel TextView
        username.text = savedUsername

        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val likes = view.findViewById<TextView>(R.id.likes)
        val userDoc = FirebaseFirestore.getInstance().collection("User").document(uid)

        userDoc.get().addOnSuccessListener { document ->
            val likeCount = document.getLong("likes") ?: 0L
            likeCount.toString()
            likes.text = "Likes: $likeCount "
        }

        val reviews = view.findViewById<TextView>(R.id.reviews)
        reviews.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_showReviews)
        }

        val settings = view.findViewById<TextView>(R.id.settings)
        settings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        val network = view.findViewById<TextView>(R.id.network)
        network.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_network)
        }

        val playlist = view.findViewById<TextView>(R.id.playlist)
        playlist.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_playlist)
        }

        return view
    }

}