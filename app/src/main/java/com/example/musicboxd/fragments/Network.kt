package com.example.musicboxd.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.UserAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.musicboxd.viewModels.UserViewModel
import com.google.android.material.tabs.TabLayout
import kotlin.getValue

class Network : Fragment() {
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.network, container, false)

        recyclerView = view.findViewById(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val followStatsText = view.findViewById<TextView>(R.id.followStatsTextView)

        userViewModel.loadMyBasicProfile(forceReload = true)
        userViewModel.searchResults.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        userViewModel.followers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        userViewModel.following.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }


        userViewModel.basicProfile.observe(viewLifecycleOwner) { profile ->
            val followers = profile?.user?.followers ?: 0
            val following = profile?.user?.following ?: 0
            followStatsText.text = "Followers: $followers · Following: $following"
        }

        adapter = UserAdapter(tabIndex = 0) { user ->
            val action = NetworkDirections.actionNetworkToUserProfile(user.id)
            findNavController().navigate(action)
        }

        recyclerView.adapter = adapter

        tabLayout = view.findViewById(R.id.tabLayout)

        val searchView = view.findViewById<SearchView>(R.id.searchView)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Tab Search
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.home)
                        )
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        adapter.submitList(emptyList())
                    }
                    1 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.add)
                        )
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        userViewModel.loadFollowers()
                    }
                    2 -> {
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(requireContext(), R.color.light_heavenly)
                        )
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        userViewModel.loadFollowing()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    userViewModel.performUserSearch(query) // usa il ViewModel
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }
}

