package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
        return view
    }
}