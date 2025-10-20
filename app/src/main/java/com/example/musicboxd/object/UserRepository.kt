package com.example.musicboxd.`object`

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.PlaylistItem
import com.example.musicboxd.local.Review
import com.example.musicboxd.local.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

//Intermediario tra l’interfaccia utente (UI) e Firebase Firestore

data class BasicProfileData(
    val user: User?,
    val reviews: List<Review>,
    val playlists: List<PlaylistItem>
)

//Per visualizzare Friends Activities
data class UserActivity(
    val action: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class UserWithData(
    val user: User?,
    val activities: List<UserActivity>,
    val reviews: List<Review>,
    val friendsActivities: List<ActivityItem>,
    val following: List<String>,
    val followers: List<String>
)

object UserRepository {
    private val _currentUser = MutableLiveData<UserWithData?>()
    val currentUser: LiveData<UserWithData?> = _currentUser

    suspend fun loadMyActivitiesAndFollowersActivities(): Pair<List<ActivityItem>, List<ActivityItem>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Pair(emptyList(), emptyList())
        val firestore = FirebaseFirestore.getInstance()

        // Attività personali
        val personalActivityDocs = firestore.collection("User")
            .document(uid)
            .collection("Activity")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val myActivities = personalActivityDocs.mapNotNull { doc ->
            val action = doc.getString("action") ?: return@mapNotNull null
            val timestamp = doc.getTimestamp("timestamp") ?: return@mapNotNull null
            ActivityItem(content = action, timestamp = timestamp)
        }

        // Recupera ID degli utenti seguiti
        val followingDocs = firestore.collection("User")
            .document(uid)
            .collection("followingList")
            .get()
            .await()

        val followingIds = followingDocs.map { it.id }
        val friendsActivities = mutableListOf<ActivityItem>()
        val userMap = mutableMapOf<String, String>() // uid -> username

        // Recupera username associati
        for (fId in followingIds) {
            val userSnapshot = firestore.collection("User").document(fId).get().await()
            val username = userSnapshot.getString("username") ?: "Utente"
            userMap[fId] = username
        }

        for (fId in followingIds) {
            val activityDocs = firestore.collection("User")
                .document(fId)
                .collection("ActivityForOthers")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            for (doc in activityDocs) {
                val actionType = doc.getString("actionType") ?: continue
                val sourceUserId = doc.getString("sourceUserId") ?: continue
                val targetUserId = doc.getString("targetUserId")
                val timestamp = doc.getTimestamp("timestamp") ?: continue
                val songTitle = doc.getString("songTitle")
                val artistName = doc.getString("artistName")

                val sourceName = userMap[sourceUserId] ?: "Utente"
                val content = when (actionType) {
                    "follow" -> if (targetUserId == uid) "$sourceName ha iniziato a seguirti" else null
                    "review" -> "$sourceName ha recensito \"${songTitle ?: "una canzone"}\" di ${artistName ?: "un artista"}"
                    else -> null
                }

                content?.let {
                    friendsActivities.add(ActivityItem(it, timestamp))
                }
            }
        }
        return Pair(myActivities, friendsActivities.sortedByDescending { it.timestamp })
    }

    private var myActivityListener: ListenerRegistration? = null
    private var followersActivityListeners = mutableListOf<ListenerRegistration>()

    fun observeMyActivityRealtime(onUpdate: (List<ActivityItem>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        myActivityListener?.remove()
        myActivityListener = FirebaseFirestore.getInstance()
            .collection("User")
            .document(uid)
            .collection("Activity")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val list = snapshots.mapNotNull { doc ->
                        val action = doc.getString("action") ?: return@mapNotNull null
                        val timestamp = doc.getTimestamp("timestamp") ?: return@mapNotNull null
                        ActivityItem(action, timestamp)
                    }.sortedByDescending { it.timestamp }
                    onUpdate(list)
                }
            }
    }

    fun observeFriendsActivitiesRealtime(
        followingList: List<String>,
        onUpdate: (List<ActivityItem>) -> Unit
    ) {
        followersActivityListeners.forEach { it.remove() }
        followersActivityListeners.clear()

        val firestore = FirebaseFirestore.getInstance()
        val combinedActivities = mutableListOf<ActivityItem>()

        followingList.forEach { friendId ->
            val listener = firestore.collection("User")
                .document(friendId)
                .collection("ActivityForOthers")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        firestore.collection("User").document(friendId).get()
                            .addOnSuccessListener { userDoc ->
                                val username = userDoc.getString("username") ?: "Utente"
                                val activities = snapshot.mapNotNull { doc ->
                                    val actionType = doc.getString("actionType") ?: return@mapNotNull null
                                    val timestamp = doc.getTimestamp("timestamp") ?: return@mapNotNull null
                                    val targetUserId = doc.getString("targetUserId")
                                    val songTitle = doc.getString("songTitle")
                                    val artistName = doc.getString("artistName")

                                    val content = when (actionType) {
                                        "follow" -> if (targetUserId != null)
                                            "$username ha iniziato a seguire qualcuno"
                                        else null
                                        "review" -> "$username ha recensito \"${songTitle ?: "una canzone"}\" di ${artistName ?: "un artista"}"
                                        else -> null
                                    }
                                    content?.let { ActivityItem(it, timestamp) }
                                }
                                synchronized(combinedActivities) {
                                    combinedActivities.removeAll { it.content.startsWith(username) }
                                    combinedActivities.addAll(activities)
                                    onUpdate(combinedActivities.sortedByDescending { it.timestamp })
                                }
                            }
                    }
                }
            followersActivityListeners.add(listener)
        }
    }

    fun observeUserReviewsRealtime(
        userId: String,
        onUpdate: (List<Review>) -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .collection("Reviews")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val reviews = snapshot.mapNotNull { doc ->
                        val documentId = doc.id
                        val songTitle = doc.getString("title") ?: return@mapNotNull null
                        val artistName = doc.getString("artist") ?: return@mapNotNull null
                        val timestamp = doc.getTimestamp("timestamp")
                        val rating = doc.getDouble("rating") ?: 0.0
                        val reviewText = doc.getString("textReview") ?: ""
                        val cover = doc.getString("cover") ?: ""
                        Review(
                            documentId, "review", artistName, songTitle, userId,
                            cover, rating, reviewText, timestamp
                        )
                    }
                    onUpdate(reviews.sortedByDescending { it.timestamp })
                }
            }
    }

    fun observeUserPlaylistsRealtime(
        userId: String,
        onUpdate: (List<PlaylistItem>) -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .collection("Playlists")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val playlists = snapshot.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val createdBy = doc.getString("createdBy") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")
                        val tracks = doc.get("tracks") as? List<String> ?: emptyList()
                        PlaylistItem(id, name, createdBy, timestamp, tracks)
                    }
                    onUpdate(playlists.sortedByDescending { it.timestamp })
                }
            }
    }


    //Recupera Recensioni degli Utenti per ShowUserReviews
    suspend fun loadReviewsForUser(userId: String): List<Review> {
        val firestore = FirebaseFirestore.getInstance()
        val reviewDocs = firestore
            .collection("User")
            .document(userId)
            .collection("Reviews")
            .get()
            .await()

        return reviewDocs.mapNotNull { doc ->
            val documentId = doc.id
            val songTitle = doc.getString("title") ?: return@mapNotNull null
            val artistName = doc.getString("artist") ?: return@mapNotNull null
            val timestamp = doc.getTimestamp("timestamp")
            val rating = doc.getDouble("rating") ?: 0.0
            val reviewText = doc.getString("textReview") ?: ""
            val cover = doc.getString("cover") ?: ""

            Review(
                documentId = documentId,
                actionType = "review",
                artistName = artistName,
                songTitle = songTitle,
                sourceUserId = userId,
                albumCoverUrl = cover,
                rating = rating,
                reviewText = reviewText,
                timestamp = timestamp
            )
        }
    }

    //Permette di aggiornare uno specifico campo del documento utente in Firestore dal Fragment Profile
    fun updateField(
        field: String,
        value: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("User")
            .document(uid)
            .set(mapOf(field to value), SetOptions.merge())
            .addOnSuccessListener {
                val current = _currentUser.value
                val oldUser = current?.user
                val updatedUser = oldUser?.copy(
                    username = if (field == "username") value else oldUser.username,
                    firstName = if (field == "firstName") value else oldUser.firstName,
                    lastName = if (field == "lastName") value else oldUser.lastName
                )
                if (updatedUser != null && current != null) {
                    _currentUser.postValue(
                        UserWithData(
                            user = updatedUser,
                            activities = current.activities,
                            reviews = current.reviews,
                            friendsActivities = current.friendsActivities,
                            followers = current.followers,
                            following = current.following
                        )
                    )
                }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    //Recupero dati profilo loggato (Utente, Reviews, Playlist)
    suspend fun loadMyBasicDataWithReviewsAndPlaylists(): BasicProfileData {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return BasicProfileData(null, emptyList(), emptyList())
        val firestore = FirebaseFirestore.getInstance()

        // Documento principale
        val userDoc = firestore.collection("User")
            .document(uid)
            .get()
            .await()
        val user = userDoc.toObject(User::class.java)

        // Recensioni
        val reviewDocs = firestore.collection("User")
            .document(uid)
            .collection("Reviews")
            .get()
            .await()

        val reviews = reviewDocs.mapNotNull { doc ->
            val documentId = doc.id
            val songTitle = doc.getString("title") ?: return@mapNotNull null
            val artistName = doc.getString("artist") ?: return@mapNotNull null
            val timestamp = doc.getTimestamp("timestamp")
            val rating = doc.getDouble("rating") ?: 0.0
            val reviewText = doc.getString("textReview") ?: ""
            val cover = doc.getString("cover") ?: ""

            Review(
                documentId = documentId,
                actionType = "review",
                artistName = artistName,
                songTitle = songTitle,
                sourceUserId = uid,
                albumCoverUrl = cover,
                rating = rating,
                reviewText = reviewText,
                timestamp = timestamp
            )
        }

        // Playlist
        val playlistDocs = firestore.collection("User")
            .document(uid)
            .collection("Playlists")
            .get()
            .await()

        val playlists = playlistDocs.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val createdBy = doc.getString("createdBy") ?: ""
            val timestamp = doc.getTimestamp("timestamp")
            val tracks = doc.get("tracks") as? List<String> ?: emptyList()

            PlaylistItem(
                id = id,
                name = name,
                createdBy = createdBy,
                timestamp = timestamp,
                tracks = tracks
            )
        }

        return BasicProfileData(user, reviews, playlists)
    }

    fun observeUserDocument(userId: String, onChange: (User?) -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("User")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                onChange(user)
            }
    }

    //Recupero dati profilo di un utente qualsiasi (Utente, reviews, playlist)
    suspend fun loadUserBasicDataWithReviewsAndPlaylists(userId: String): BasicProfileData {
        val firestore = FirebaseFirestore.getInstance()

        // Documento principale dell’utente
        val userDoc = firestore.collection("User")
            .document(userId)
            .get()
            .await()
        val user = userDoc.toObject(User::class.java)

        // Recensioni dell’utente
        val reviewDocs = firestore.collection("User")
            .document(userId)
            .collection("Reviews")
            .get()
            .await()

        val reviews = reviewDocs.mapNotNull { doc ->
            val documentId = doc.id
            val songTitle = doc.getString("title") ?: return@mapNotNull null
            val artistName = doc.getString("artist") ?: return@mapNotNull null
            val timestamp = doc.getTimestamp("timestamp")
            val rating = doc.getDouble("rating") ?: 0.0
            val reviewText = doc.getString("textReview") ?: ""
            val cover = doc.getString("cover") ?: ""

            Review(
                documentId = documentId,
                actionType = "review",
                artistName = artistName,
                songTitle = songTitle,
                sourceUserId = userId,
                albumCoverUrl = cover,
                rating = rating,
                reviewText = reviewText,
                timestamp = timestamp
            )
        }

        // Playlist dell’utente
        val playlistDocs = firestore.collection("User")
            .document(userId)
            .collection("Playlists")
            .get()
            .await()

        val playlists = playlistDocs.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val createdBy = doc.getString("createdBy") ?: ""
            val timestamp = doc.getTimestamp("timestamp")
            val tracks = doc.get("tracks") as? List<String> ?: emptyList()

            PlaylistItem(id, name, createdBy, timestamp, tracks)
        }

        return BasicProfileData(user, reviews, playlists)
    }

    suspend fun searchUsersByUsername(query: String): List<User> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("User")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()

        return snapshot.map { it.toObject(User::class.java).copy(id = it.id) }
    }

    // Carica i follower dell’utente loggato
    suspend fun getFollowers(): List<User> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val db = FirebaseFirestore.getInstance()

        val followerIds = db.collection("User").document(uid)
            .collection("followersList")
            .get().await()
            .map { it.id }

        if (followerIds.isEmpty()) return emptyList()

        val snapshot = db.collection("User")
            .whereIn(FieldPath.documentId(), followerIds)
            .get()
            .await()

        return snapshot.mapNotNull { it.toObject(User::class.java).copy(id = it.id) }
    }

    // Carica gli utenti seguiti
    suspend fun getFollowing(): List<User> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val db = FirebaseFirestore.getInstance()

        val followingIds = db.collection("User").document(uid)
            .collection("followingList")
            .get().await()
            .map { it.id }

        if (followingIds.isEmpty()) return emptyList()

        val snapshot = db.collection("User")
            .whereIn(FieldPath.documentId(), followingIds)
            .get().await()

        return snapshot.mapNotNull { it.toObject(User::class.java).copy(id = it.id) }
    }


}
