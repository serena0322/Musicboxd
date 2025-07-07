package com.example.musicboxd.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import com.example.musicboxd.local.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserAdapter(
    private var tabIndex: Int = 0,
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    fun updateTabIndex(index: Int) {
        tabIndex = index
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            itemView.findViewById<TextView>(R.id.username).text = user.username
            val firstAndLast = "${user.firstName} ${user.lastName}"
            itemView.findViewById<TextView>(R.id.name).text = firstAndLast


            itemView.setOnLongClickListener {
                showPopupMenu(it, user)
                true
            }

            itemView.setOnClickListener {
                onUserClick(user)
            }

        }
    }

    //pulsante follow
    private fun showPopupMenu(view: View, user: User) {
        val popup = PopupMenu(view.context, view)

        when (tabIndex) {
            0 -> popup.inflate(R.menu.user_item_menu)        // Follow
            2 -> popup.inflate(R.menu.user_following_menu)   // Unfollow
        }

        popup.setOnMenuItemClickListener { menuItem ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: return@setOnMenuItemClickListener false

            if (user.id == currentUserId) {
                Toast.makeText(view.context, "Non puoi seguire te stesso", Toast.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener false
            }

            when (menuItem.itemId) {
                R.id.follow_user -> {
                    followUser(user.id) { success ->
                        if (success) {
                            logUserActivityforOthers("follow", targetUserId = user.id)
                            val logMsg = "Hai iniziato a seguire ${user.username}"
                            logUserActivity(logMsg)
                            Toast.makeText(view.context, "Ora segui ${user.username}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(view.context, "Segui già ${user.username}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                R.id.unfollow_user -> {
                    unfollowUser(user.id)
                    logUserActivityforOthers("unfollow", targetUserId = user.id)
                    val logMsg = "Hai smesso di seguire ${user.username}"
                    logUserActivity(logMsg)
                    Toast.makeText(view.context, "Non segui più ${user.username}", Toast.LENGTH_SHORT).show()
                }
            }

            return@setOnMenuItemClickListener true
        }

        popup.show()
    }

    private fun logUserActivityforOthers(actionType: String, targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val activity = hashMapOf(
            "actionType" to actionType,
            "sourceUserId" to currentUserId,
            "targetUserId" to targetUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // SALVA L’ATTIVITÀ NEL DOCUMENTO DELL’UTENTE TARGET (cioè quello seguito)
        FirebaseFirestore.getInstance()
            .collection("User")
            .document(targetUserId)
            .collection("ActivityForOthers")
            .add(activity)

    }

    private fun logUserActivity(actionMessage: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val activity = hashMapOf(
            "action" to actionMessage,
            "timestamp" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("User")
            .document(currentUserId)
            .collection("Activity")
            .add(activity)
    }

    //funzione follow
    private fun followUser(targetUserId: String, onResult: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val targetUserRef = db.collection("User").document(targetUserId)
        val currentUserRef = db.collection("User").document(currentUserId)

        val followerDocRef = targetUserRef.collection("followersList").document(currentUserId)
        val followingDocRef = currentUserRef.collection("followingList").document(targetUserId)

        followerDocRef.get().addOnSuccessListener { followerSnapshot ->
            if (followerSnapshot.exists()) {
                Log.d("Follow", "Utente già tra i followers: operazione ignorata")
                onResult(false) // <-- follow NON eseguito
                return@addOnSuccessListener
            }

            Tasks.whenAllSuccess<DocumentSnapshot>(
                currentUserRef.get(),
                targetUserRef.get()
            ).addOnSuccessListener { snapshots ->
                val currentUserDoc = snapshots[0]
                val targetUserDoc = snapshots[1]

                val updates = mutableListOf<Task<Void>>()

                if (!currentUserDoc.exists() || currentUserDoc.getLong("following") == null) {
                    updates.add(currentUserRef.set(mapOf("following" to 0L), SetOptions.merge()))
                }

                if (!targetUserDoc.exists() || targetUserDoc.getLong("followers") == null) {
                    updates.add(targetUserRef.set(mapOf("followers" to 0L), SetOptions.merge()))
                }

                Tasks.whenAll(updates).addOnSuccessListener {
                    val batch = db.batch()

                    batch.set(followerDocRef, mapOf("followedAt" to Timestamp.now()))
                    batch.set(followingDocRef, mapOf("followedAt" to Timestamp.now()))

                    batch.update(targetUserRef, "followers", FieldValue.increment(1L))
                    batch.update(currentUserRef, "following", FieldValue.increment(1L))

                    batch.commit().addOnSuccessListener {
                        Log.d("Follow", "Follow eseguito correttamente")
                        onResult(true) // <-- follow eseguito
                    }.addOnFailureListener { e ->
                        Log.e("Follow", "Errore nel commit", e)
                        onResult(false)
                    }
                }.addOnFailureListener { e ->
                    Log.e("Follow", "Errore aggiornamento contatori", e)
                    onResult(false)
                }

            }.addOnFailureListener { e ->
                Log.e("Follow", "Errore nel recupero documenti", e)
                onResult(false)
            }

        }.addOnFailureListener { e ->
            Log.e("Follow", "Errore nel controllo follower", e)
            onResult(false)
        }
    }



    //unfollow
    private fun unfollowUser(targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val targetUserRef = db.collection("User").document(targetUserId)
        val currentUserRef = db.collection("User").document(currentUserId)

        val followerDocRef = targetUserRef.collection("followersList").document(currentUserId)
        val followingDocRef = currentUserRef.collection("followingList").document(targetUserId)

        val batch = db.batch()

        batch.delete(followerDocRef)
        batch.delete(followingDocRef)

        batch.update(targetUserRef, "followers", FieldValue.increment(-1))
        batch.update(currentUserRef, "following", FieldValue.increment(-1))

        batch.commit()
            .addOnSuccessListener {
                // Rimuove utente dalla lista corrente
                val currentList = currentList.toMutableList()
                currentList.removeAll { it.id == targetUserId }
                submitList(currentList)
            }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id // usa un campo univoco
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

