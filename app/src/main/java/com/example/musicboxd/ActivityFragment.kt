package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.adapter.ActivityAdapter
import com.google.android.material.tabs.TabLayout

class ActivityFragment: Fragment(){

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityAdapter

    @SuppressLint("ServiceCast", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflazione del layout
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Dati iniziali (Friends tab)
        val friendsList = listOf(
            ActivityItem("Marco liked your post"),
            ActivityItem("Elena followed you"),
            ActivityItem("Luca commented your list")
        )

        adapter = ActivityAdapter(friendsList)
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val (colorRes, data) = when (tab.position) {
                    0 -> Pair(R.color.home, listOf(
                        ActivityItem("Marco liked your post"),
                        ActivityItem("Elena followed you"),
                        ActivityItem("Luca commented your list")
                    ))
                    1 -> Pair(R.color.add, listOf(
                        ActivityItem("You posted a new journal entry"),
                        ActivityItem("You added an album to a list")
                    ))
                    2 -> Pair(R.color.teal_200, listOf(
                        ActivityItem("Request from Giulia"),
                        ActivityItem("Francesco wants to follow you")
                    ))
                    else -> Pair(R.color.home, emptyList())
                }
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), colorRes)
                )
                adapter.updateData(data) // aggiorna il contenuto della RecyclerView
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        return view
    }
}