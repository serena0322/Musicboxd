package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.musicboxd.R
import com.example.musicboxd.viewModels.UserViewModel

class ProfileFragment : Fragment() {

    //activityViewModels --> quando vuoi condividere dati tra Activity e i suoi fragment
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.observeMyProfileDataRealtime()

        val usernameTextView = view.findViewById<TextView>(R.id.Title)
        val likesTextView = view.findViewById<TextView>(R.id.likes)
        val reviewsTextView = view.findViewById<TextView>(R.id.reviews)
        val playlistsTextView = view.findViewById<TextView>(R.id.playlist)

        userViewModel.basicProfile.observe(viewLifecycleOwner) { profile ->
            if (profile?.user == null) return@observe
            val user = profile.user
            val likes = user.likes
            val reviewCount = profile.reviews.size
            val playlistCount = profile.playlists.size

            usernameTextView.text = user?.username ?: "Guest"
            likesTextView.text = "Likes: $likes"
            reviewsTextView.text = "Reviews: $reviewCount"
            playlistsTextView.text = "Playlists: $playlistCount"
        }

    //Navigazioni
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
    }
}
