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
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

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
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Colore del testo digitato
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Colore dell'hint

        // Imposta i listener per i TextView, se necessario
        view.findViewById<TextView>(R.id.releaseDate).setOnClickListener {
            // Logica per il click sul TextView "releaseDate"
        }
        view.findViewById<TextView>(R.id.genreCountryLanguage).setOnClickListener {
            // Logica per il click sul TextView "genreCountryLanguage"
        }

        // Trova la RecyclerView nel layout
        recyclerView = view.findViewById(R.id.searchRecyclerView)

        // Inizialmente nascondi la RecyclerView
        recyclerView.visibility = View.GONE

        // Aggiungi un listener alla SearchView per rilevare quando l'utente inizia a digitare
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Gestisci la logica di ricerca quando l'utente invia la query
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Rendi visibile la RecyclerView quando l'utente inizia a digitare
                if (newText.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE  // Nascondi se la query è vuota
                } else {
                    recyclerView.visibility = View.VISIBLE  // Mostra se c'è del testo
                }
                return true
            }
        })

        // Restituisci la vista
        return view
    }
}
