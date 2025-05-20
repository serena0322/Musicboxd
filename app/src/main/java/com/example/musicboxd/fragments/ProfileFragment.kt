package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ProfileAdapter
import com.google.android.material.tabs.TabLayout

class ProfileFragment: Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var scrollView: ScrollView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProfileAdapter  // da creare
    private val layoutManager by lazy { LinearLayoutManager(requireContext()) }

    @SuppressLint("ServiceCast", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflazione del layout
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Trova il TextView dal layout
        val username: TextView = view.findViewById(R.id.Title)
        // Recupera l'username da SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("saved_username", "Guest")  // "Guest" è il valore predefinito nel caso non sia stato salvato nessun username
        // Imposta l'username nel TextView
        username.text = savedUsername

        tabLayout = view.findViewById(R.id.tabLayout)
        scrollView = view.findViewById(R.id.scrollView)
        recyclerView = view.findViewById(R.id.profileRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ProfileAdapter(emptyList())

        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val context = requireContext()
                when (tab.position) {
                    0 -> {
                        // Profile
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.home))
                        scrollView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                    1 -> {
                        // Diary
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.add))
                        scrollView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(
                            listOf("Diario 1", "Appunto 2", "Nota 3")
                        )
                    }

                    2 -> {
                        // Lists
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.teal_200))
                        scrollView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(
                            listOf("Lista Album Preferiti", "Top 10 Canzoni", "Playlist Chill")
                        )
                    }

                    3 -> {
                        // Watchlist
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.profile))
                        scrollView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(
                            listOf("Album da ascoltare", "Da riascoltare", "Nuove uscite")
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val settings = view.findViewById<TextView>(R.id.settings)
        settings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        val music = view.findViewById<TextView>(R.id.music)
        music.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ratedMusic)
        }

        return view
    }
}