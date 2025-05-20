package com.example.musicboxd.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicboxd.R


class SearchFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        recyclerView = view.findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.visibility = View.GONE

        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        return view
    }
}
