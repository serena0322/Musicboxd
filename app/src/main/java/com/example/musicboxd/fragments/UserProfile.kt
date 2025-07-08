package com.example.musicboxd.fragments

import android.annotation.SuppressLint
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
import com.example.musicboxd.`object`.UserRepository

class UserProfile : Fragment() {

    private lateinit var userId: String
    private lateinit var usernameTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var followingCountTextView: TextView
    private lateinit var likesTextView: TextView
    private val args: UserProfileArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userId = args.userId
        return inflater.inflate(R.layout.user_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTextView = view.findViewById(R.id.username)
        followersCountTextView = view.findViewById(R.id.followers)
        followingCountTextView = view.findViewById(R.id.following)
        likesTextView = view.findViewById(R.id.likes)

        val playlist = view.findViewById<TextView>(R.id.playlist)
        val reviews = view.findViewById<TextView>(R.id.reviews)

        playlist.setOnClickListener {
            val action = UserProfileDirections.actionUserProfileToUserplaylist(userId)
            findNavController().navigate(action)
        }

        reviews.setOnClickListener {
            val action = UserProfileDirections.actionUserProfileToShowUserReviews(userId)
            findNavController().navigate(action)
        }

        UserRepository.loadUserProfile(
            userId = userId,
            onSuccess = { profile ->
                usernameTextView.text = profile.username
                followersCountTextView.text = "Followers: ${profile.followersCount}"
                followingCountTextView.text = "Following: ${profile.followingCount}"
                likesTextView.text = "Likes: ${profile.likes}"
            },
            onFailure = { e ->
                Log.e("UserProfile", "Errore nel caricamento profilo", e)
                usernameTextView.text = "Unknown"
                followersCountTextView.text = "Followers: 0"
                followingCountTextView.text = "Following: 0"
                likesTextView.text = "Likes: 0"
            }
        )
    }
}
