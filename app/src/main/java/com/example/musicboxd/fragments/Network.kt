package com.example.musicboxd.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.musicboxd.adapter.UserAdapter
import com.example.musicboxd.local.User
import com.google.android.gms.tasks.Tasks
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class Network : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.network, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter(tabIndex = 0) { user ->
            val bundle = Bundle().apply {
                putString("USER_ID", user.id)
            }

            findNavController().navigate(R.id.action_network_to_userProfile, bundle)
        }

        recyclerView.adapter = adapter

        tabLayout = view.findViewById(R.id.tabLayout)

        val searchView = view.findViewById<SearchView>(R.id.searchView)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Tab "Search"
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        adapter.submitList(emptyList()) // Pulisci la lista o mostra tutto
                    }

                    1 -> { // Tab "Followers"
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        loadFollowers() // funzione per caricare i follower
                    }

                    2 -> { // Tab "Following"
                        adapter.updateTabIndex(tab.position)
                        searchView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                        loadFollowing() // funzione per caricare i following                    }
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
                    searchUsersByUsername(query)
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchUsersByUsername(query: String) {
        Firebase.firestore.collection("User")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.map { it.toObject(User::class.java).copy(id = it.id) }
                adapter.submitList(users)
            }
    }

    private fun loadFollowers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val followersRef = db.collection("User")
            .document(currentUserId)
            .collection("followersList")

        followersRef.get().addOnSuccessListener { documents ->
            val followerIds = documents.mapNotNull { it.id }

            if (followerIds.isEmpty()) {
                adapter.submitList(emptyList())
                return@addOnSuccessListener
            }

            db.collection("User")
                .whereIn(FieldPath.documentId(), followerIds)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    adapter.submitList(userList)
                }
                .addOnFailureListener { e ->
                    Log.e("loadFollowers", "Errore nel recupero utenti: ", e)
                }

        }.addOnFailureListener { e ->
            Log.e("loadFollowers", "Errore nel recupero follower: ", e)
        }
    }

    private fun loadFollowing() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("User").document(currentUserId)
            .collection("followingList").get()
            .addOnSuccessListener { docs ->
                val ids = docs.map { it.id }

                if (ids.isEmpty()) {
                    adapter.submitList(emptyList())
                    return@addOnSuccessListener
                }

                val userRef = db.collection("User")
                val tasks = ids.map { userRef.document(it).get() }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                    .addOnSuccessListener { snapshots ->
                        val users = snapshots.mapNotNull { it.toObject(User::class.java) }
                        adapter.submitList(users)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Following", "Errore nel recupero utenti", e)
                        adapter.submitList(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Following", "Errore nel recupero followingList", e)
                adapter.submitList(emptyList())
            }
    }

}

