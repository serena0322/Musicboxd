package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.musicboxd.viewModels.UserViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicboxd.R
class UserProfile : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var reviewsCountTextView: TextView
    private lateinit var playlistsCountTextView: TextView
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

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameTextView = view.findViewById(R.id.username)
        followersCountTextView = view.findViewById(R.id.followers)
        followingCountTextView = view.findViewById(R.id.following)
        likesTextView = view.findViewById(R.id.likes)
        reviewsCountTextView = view.findViewById(R.id.reviews)
        playlistsCountTextView = view.findViewById(R.id.playlist)

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

        // Esegui il caricamento dati
        userViewModel.loadOtherUserProfile(userId)

        // prima dell'osservazione
        usernameTextView.text = "Loading..."
        followersCountTextView.text = "..."
        followingCountTextView.text = "..."
        likesTextView.text = "..."

        userViewModel.otherUserProfile.observe(viewLifecycleOwner) { profileData ->
            val user = profileData.user

            if (user != null) {
                usernameTextView.text = user.username
                followersCountTextView.text = "Followers: ${user.followers }"
                followingCountTextView.text = "Following: ${user.following }"
                likesTextView.text = "Likes: ${user.likes}"
            } else {
                usernameTextView.text = "Utente non trovato"
                followersCountTextView.text = "Followers: 0"
                followingCountTextView.text = "Following: 0"
                likesTextView.text = "Likes: 0"
            }

            // Se vuoi mostrare anche il numero di recensioni o playlist
            val reviewsCount = profileData.reviews.size
            val playlistsCount = profileData.playlists.size

            reviewsCountTextView.text = "Recensioni: $reviewsCount"
            playlistsCountTextView.text = "Playlist: $playlistsCount"
        }
    }
}

