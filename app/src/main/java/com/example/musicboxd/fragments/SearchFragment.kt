package com.example.musicboxd.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.musicboxd.viewModels.SearchViewModel
import com.example.musicboxd.R
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.example.musicboxd.adapter.TrackResponse
import com.example.musicboxd.fragments.InformationActivity
import com.example.musicboxd.classes.Track

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: TrackResponse

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TrackResponse { selectedTrack ->
            val intent = Intent(requireContext(), InformationActivity::class.java).apply {
                putExtra("track", selectedTrack)
                putExtra("cover", selectedTrack.album?.cover)
            }
            startActivity(intent)
        }

        adapter = TrackResponse { selectedTrack ->
            val intent = Intent(requireContext(), InformationActivity::class.java).apply {
                putExtra("track", selectedTrack)
                putExtra("cover", selectedTrack.album?.cover)
            }
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.searchScrollView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.search(query)
                    //nasconde la tastiera dopo la ricerca
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            adapter.submitList(tracks as List<Track?>?)
        }
    }
}
