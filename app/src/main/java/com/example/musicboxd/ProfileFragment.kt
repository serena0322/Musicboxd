package com.example.musicboxd

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout

class ProfileFragment: Fragment() {
    private lateinit var tabLayout: TabLayout
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
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Tab Profile cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.home
                            )
                        )
                    }

                    1 -> {
                        // Tab Diary cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.add
                            )
                        )
                    }

                    2 -> {
                        // Tab List cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.teal_200
                            )
                        )
                    }
                    3 -> {
                        // Tab Watchlist cliccato
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.profile
                            )
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Opzionale: azioni quando il tab viene deselezionato
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Opzionale: azioni quando il tab viene riselezionato
            }
        })

        val settings = view.findViewById<TextView>(R.id.settings)
        settings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        return view
    }
}