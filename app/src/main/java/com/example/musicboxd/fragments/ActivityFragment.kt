package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ActivityAdapter
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.`object`.UserRepository
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

        adapter = ActivityAdapter(0,emptyList())
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val userData = UserRepository.currentUser.value
                when (tab.position) {
                    0 -> { // Amici
                        val friendsActivities = userData?.friendsActivities
                            ?.sortedByDescending { it.timestamp }
                            ?: emptyList()
                        adapter.updateTabIndex(0)
                        adapter.updateData(friendsActivities)
                    }
                    1 -> { // Tu
                        val yourActivities = userData?.activities
                            ?.sortedByDescending { it.timestamp }
                            ?.map { ActivityItem(it.action, it.timestamp) }
                            ?: emptyList()
                        adapter.updateTabIndex(1)
                        adapter.updateData(yourActivities)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        tabLayout.post {
            val initialTab = tabLayout.getTabAt(1)
            initialTab?.select()
            adapter.updateTabIndex(1)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UserRepository.currentUser.observe(viewLifecycleOwner) { userData ->
            val activitiesToDisplay = if (tabLayout.selectedTabPosition == 0) {
                userData?.friendsActivities ?: emptyList()
            } else {
                userData?.activities?.map {
                    ActivityItem(it.action, it.timestamp)
                } ?: emptyList()
            }

            adapter.updateData(activitiesToDisplay)
            adapter.updateTabIndex(tabLayout.selectedTabPosition)
        }
    }




}