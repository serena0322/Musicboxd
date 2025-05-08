package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflaziona il layout
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Inizializza la SearchView
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        // Personalizza il testo di ricerca e l'hint
        searchView.queryHint = "Search with Musicboxd..." // Imposta l'hint

        // Inizializza EditText interno della SearchView
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        // Cambia il colore del testo e dell'hint tramite EditText
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // colore del testo digitato
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // colore dell'hint

        view.findViewById<TextView>(R.id.releaseDate).setOnClickListener {
            //al click
        }
        view.findViewById<TextView>(R.id.genreCountryLanguage).setOnClickListener {
            //al click
        }

        // Restituisci la vista
        return view
    }

}