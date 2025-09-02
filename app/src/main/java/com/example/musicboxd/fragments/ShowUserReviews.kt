package com.example.musicboxd.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ReviewAdapter
import com.example.musicboxd.local.Review
import com.example.musicboxd.viewModels.UserViewModel

class ShowUserReviews : Fragment() {

    private val args: ShowUserReviewsArgs by navArgs()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.show_reviews, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = ReviewAdapter(reviews = reviewList, onDeleteClick = { }, showAuthor = false)


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Avvia caricamento tramite ViewModel
        userViewModel.loadReviewsForUser(args.userId)

        // Osserva i dati appena caricati
        userViewModel.otherUserReviews.observe(viewLifecycleOwner) { reviews ->
            reviewList.clear()
            reviewList.addAll(reviews)
            adapter.notifyDataSetChanged()
        }

        return view
    }
}
