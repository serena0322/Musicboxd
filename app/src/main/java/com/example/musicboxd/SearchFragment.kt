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
import android.widget.Toast

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
        searchView.queryHint = "Cerca su Musicboxd..." // Imposta l'hint

        // Inizializza EditText interno della SearchView
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        // Cambia il colore del testo e dell'hint tramite EditText
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // colore del testo digitato
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // colore dell'hint

        view.findViewById<TextView>(R.id.releaseDate).setOnClickListener {
            Toast.makeText(requireContext(), "Release date clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<TextView>(R.id.genreCountryLanguage).setOnClickListener {
            Toast.makeText(requireContext(), "Journal clicked", Toast.LENGTH_SHORT).show()
        }

        // Restituisci la vista
        return view
    }

}