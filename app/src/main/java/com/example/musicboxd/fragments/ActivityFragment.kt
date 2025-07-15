package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ActivityAdapter
import com.example.musicboxd.viewModels.UserViewModel
import com.google.android.material.tabs.TabLayout

class ActivityFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ActivityAdapter(0, emptyList())
        recyclerView.adapter = adapter

        // Imposta il tab iniziale (Tab 0: attività amici)
        tabLayout.post {
            tabLayout.getTabAt(0)?.select()
            adapter.updateTabIndex(0)
        }

        userViewModel.observeAllActivitiesRealtime()

        // Listener cambio tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateDisplayedActivities(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Osserva attività in tempo reale
        userViewModel.friendsActivities.observe(viewLifecycleOwner) {
            if (tabLayout.selectedTabPosition == 0) {
                adapter.updateTabIndex(0)
                adapter.updateData(it)
            }
        }

        userViewModel.myActivities.observe(viewLifecycleOwner) {
            if (tabLayout.selectedTabPosition == 1) {
                adapter.updateTabIndex(1)
                adapter.updateData(it)
            }
        }
    }

    private fun updateDisplayedActivities(tabPosition: Int) {
        when (tabPosition) {
            0 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.home)
                )
                val data = userViewModel.friendsActivities.value ?: emptyList()
                adapter.updateTabIndex(0)
                adapter.updateData(data)
            }
            1 -> {
                tabLayout.setSelectedTabIndicatorColor(
                    ContextCompat.getColor(requireContext(), R.color.light_heavenly)
                )
                val data = userViewModel.myActivities.value ?: emptyList()
                adapter.updateTabIndex(1)
                adapter.updateData(data)
            }
        }
    }
}
