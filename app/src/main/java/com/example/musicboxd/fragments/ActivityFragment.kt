package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.ActivityAdapter
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.RawActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.security.Timestamp

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
                when (tab.position) {
                    0 -> { //Friends
                        adapter.updateTabIndex(tab.position)
                        recyclerView.visibility = View.VISIBLE
                        loadFriendsActivities()
                    }
                    1 -> { //You
                        adapter.updateTabIndex(tab.position)
                        recyclerView.visibility = View.VISIBLE
                        loadCurrentUserActivity()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Esegui comunque il caricamento iniziale se il tab corrente è già selezionato
        if (tabLayout.selectedTabPosition == 1) {
            loadCurrentUserActivity()
        } else if (tabLayout.selectedTabPosition == 0) {
            loadFriendsActivities()
        }

        return view
    }

    private fun loadCurrentUserActivity() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("User")
            .document(currentUserId)
            .collection("Activity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val activityList = documents.mapNotNull { doc ->
                    val action = doc.getString("action")
                    val timestamp = doc.getTimestamp("timestamp")
                    if (action != null) ActivityItem(action, timestamp) else null
                }
                adapter.updateData(activityList)
            }

            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nel caricamento delle attività", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadFriendsActivities() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val activityList = mutableListOf<ActivityItem>()

        db.collection("User")
            .document(currentUser.uid)
            .collection("followingList")
            .get()
            .addOnSuccessListener { followingDocs ->
                if (followingDocs.isEmpty) {
                    adapter.updateData(emptyList())
                    return@addOnSuccessListener
                }

                val tasks = mutableListOf<Task<QuerySnapshot>>()
                for (doc in followingDocs) {
                    val followedUserId = doc.id
                    if (followedUserId == currentUser.uid) continue // evita attività di se stessi

                    val task = db.collection("User")
                        .document(followedUserId)
                        .collection("ActivityForOthers")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                    tasks.add(task)
                }

                Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                    .addOnSuccessListener { snapshots ->
                        val rawActivities = mutableListOf<RawActivity>()

                        // Estrai attività dalle snapshot
                        for (snapshot in snapshots) {
                            for (activityDoc in snapshot) {
                                val actionType = activityDoc.getString("actionType") ?: continue
                                val sourceUserId = activityDoc.getString("sourceUserId") ?: continue
                                if (sourceUserId == currentUser.uid) continue // Filtra le attività dell'utente corrente

                                val targetUserId = activityDoc.getString("targetUserId")
                                val timestamp = activityDoc.getTimestamp("timestamp") ?: continue
                                val songTitle = activityDoc.getString("songTitle")
                                val artistName = activityDoc.getString("artistName")

                                rawActivities.add(RawActivity(actionType, sourceUserId, targetUserId, timestamp, songTitle, artistName))
                            }

                        }

                        // Crea un set di tutti gli userId coinvolti per fetchare username
                        val userIdsToFetch = mutableSetOf<String>()
                        for (activity in rawActivities) {
                            userIdsToFetch.add(activity.sourceUserId.toString())
                            activity.targetUserId?.let { userIdsToFetch.add(it) }
                        }

                        val userMap = mutableMapOf<String, String>()
                        val fetchTasks = userIdsToFetch.map { userId ->
                            db.collection("User").document(userId).get().continueWith { task ->
                                val doc = task.result
                                if (doc != null && doc.exists()) {
                                    userMap[userId] = doc.getString("username") ?: "Utente"
                                }
                            }
                        }

                        Tasks.whenAllComplete(fetchTasks).addOnSuccessListener {
                            // Costruisci messaggi leggendo da userMap
                            for (activity in rawActivities) {
                                val sourceUsername = userMap[activity.sourceUserId] ?: "Utente"
                                val isTargetCurrentUser = (activity.targetUserId == currentUser.uid)
                                val targetUsername = userMap[activity.targetUserId] ?: "qualcuno"
                                val songTitle = activity.songTitle ?: "una canzone"
                                val artistName = activity.artistName?: "artistName"

                                val actionMessage = when (activity.actionType) {
                                    "follow" -> if (isTargetCurrentUser) {
                                        "$sourceUsername ha iniziato a seguirti"
                                    } else {
                                        "$sourceUsername ha iniziato a seguire $targetUsername"
                                    }

                                    "unfollow" -> if (isTargetCurrentUser) {
                                        "$sourceUsername ha smesso di seguirti"
                                    } else {
                                        "$sourceUsername ha smesso di seguire $targetUsername"
                                    }
                                    "review" -> "$sourceUsername ha recensito \"$songTitle\" di $artistName"
                                    else -> "$sourceUsername ha effettuato un'azione"
                                }

                                activityList.add(ActivityItem(actionMessage, activity.timestamp))
                            }

                            // Ordina e aggiorna la lista
                            val sortedList = activityList.sortedByDescending { it.timestamp }
                            adapter.updateData(sortedList)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Errore nel recupero attività", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nel recupero dei following", Toast.LENGTH_SHORT).show()
            }
    }

}