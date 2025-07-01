package com.example.musicboxd.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter
import com.example.musicboxd.classes.Track
import com.example.musicboxd.viewModels.SearchViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.R as MaterialR

class AddSongBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializza RecyclerView e Adapter (come in SearchFragment)
        adapter = TrackAdapter() { selectedTrack ->
            val intent = Intent(requireContext(), ReviewActivity::class.java).apply {
                putExtra("title", selectedTrack.title)
                putExtra("artist", selectedTrack.artist?.name)
                putExtra("cover", selectedTrack.album?.cover)
            }
            startActivity(intent)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.addScrollView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configura SearchView
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.search(query)
                    // Nasconde la tastiera dopo la ricerca
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        // Osserva i risultati della ricerca
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            adapter.submitList(tracks as List<Track?>?)
        }
    }

    override fun onStart() {
        super.onStart()
        val view = dialog?.findViewById<View>(MaterialR.id.design_bottom_sheet)
        view?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.90).toInt()
            it.layoutParams = layoutParams
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(MaterialR.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.bg_bottom_sheet_rounded)
        }
        return dialog
    }
}
