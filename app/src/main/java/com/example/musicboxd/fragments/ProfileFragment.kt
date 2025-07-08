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
import com.example.musicboxd.`object`.UserRepository
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment: Fragment() {

    @SuppressLint("ServiceCast", "MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val usernameText = view.findViewById<TextView>(R.id.Title)
        val likesText = view.findViewById<TextView>(R.id.likes)

        val userData = UserRepository.currentUser.value
        val user = userData?.user
        val likes = user?.likes ?: 0

        usernameText.text = user?.username ?: "Guest"
        likesText.text = "Likes: $likes"

        // Navigazioni
        view.findViewById<TextView>(R.id.reviews).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_showReviews)
        }
        view.findViewById<TextView>(R.id.settings).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        view.findViewById<TextView>(R.id.network).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_network)
        }
        view.findViewById<TextView>(R.id.playlist).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_playlist)
        }

        return view
    }


}