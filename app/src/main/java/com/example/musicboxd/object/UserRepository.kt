package com.example.musicboxd.`object`

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.Review
import com.example.musicboxd.local.UserActivity
import com.example.musicboxd.local.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

//Intermediario tra l’interfaccia utente (UI) e Firebase Firestore

data class UserProfileData(
    val username: String,
    val likes: Long,
    val followersCount: Int,
    val followingCount: Int
)

//Per visualizzare Friends Activities
data class UserActivity(
    val action: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class UserWithData(
    val user: User?,
    val activities: List<com.example.musicboxd.`object`.UserActivity>,
    val reviews: List<Review>,
    val friendsActivities: List<ActivityItem>,
    val following: List<String>,
    val followers: List<String>
)

object UserRepository {
    private val _currentUser = MutableLiveData<UserWithData?>()
    val currentUser: LiveData<UserWithData?> = _currentUser

    //Recupero dei dati personali dell'utente (User, attività, recensioni, following, followers).
    suspend fun loadUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        // Recupero utente
        val userDoc = firestore
            .collection("User")
            .document(uid)
            .get()
            .await()
        val user = userDoc.toObject(User::class.java)

        // Recupero attività proprie
        val activityDocs = firestore
            .collection("User")
            .document(uid)
            .collection("Activity")
            .get()
            .await()
        val activities = activityDocs.mapNotNull { doc ->
            val action = doc.getString("action")
            val timestamp = doc.getTimestamp("timestamp")
            if (action != null && timestamp != null) {
                UserActivity(action = action, timestamp = timestamp)
            } else null
        }

        // Recupero recensioni
        val reviewDocs = firestore
            .collection("User")
            .document(uid)
            .collection("Review")
            .get()
            .await()
        val reviews = reviewDocs.mapNotNull { it.toObject(Review::class.java) }

        // Recupero following
        val followingSnapshot = firestore.collection("User")
            .document(uid)
            .collection("followingList")
            .get()
            .await()
        val following = followingSnapshot.documents.map { it.id }

        // Recupero followers
        val followersSnapshot = firestore
            .collection("User")
            .document(uid)
            .collection("followersList")
            .get()
            .await()
        val followers = followersSnapshot.documents.map { it.id }


        //Per il tab "Friends"
        val rawActivities = mutableListOf<UserActivity>()
        val userIdSet = mutableSetOf<String>()

        for (doc in followingSnapshot) {
            val followedUserId = doc.id
            if (followedUserId == uid) continue // già corretto

            val friendActivitiesSnapshot = firestore
                .collection("User")
                .document(followedUserId)
                .collection("ActivityForOthers")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            for (activityDoc in friendActivitiesSnapshot) {
                val actionType = activityDoc.getString("actionType") ?: continue
                val sourceUserId = activityDoc.getString("sourceUserId") ?: continue

                // Escludi se sourceUserId == uid (cioè attività tue stesse)
                if (sourceUserId == uid) continue

                val timestamp = activityDoc.getTimestamp("timestamp") ?: continue
                val songTitle = activityDoc.getString("songTitle")
                val artistName = activityDoc.getString("artistName")
                val targetUserId = activityDoc.getString("targetUserId")

                userIdSet.add(sourceUserId)
                if (targetUserId != null) userIdSet.add(targetUserId)

                rawActivities.add(
                    UserActivity(
                        actionType = actionType,
                        sourceUserId = sourceUserId,
                        targetUserId = targetUserId,
                        timestamp = timestamp,
                        songTitle = songTitle,
                        artistName = artistName
                    )
                )
            }
        }

        // Recupera username da Firestore
        val userMap = mutableMapOf<String, String>()
        val userDocTasks = userIdSet.map { userId ->
            firestore.collection("User").document(userId).get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(userDocTasks)
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    if (doc.exists()) {
                        val id = doc.id
                        val username = doc.getString("username") ?: "Utente"
                        userMap[id] = username
                    }
                }

                // Dopo aver popolato userMap, costruisci i messaggi leggibili
                val friendsActivities = rawActivities.mapNotNull { activity ->
                    val sourceName = userMap[activity.sourceUserId] ?: "Utente"

                    val message = when (activity.actionType) {
                        "follow" -> if (activity.targetUserId == uid)
                            "$sourceName ha iniziato a seguirti"
                        else return@mapNotNull null

                        "review" -> "$sourceName ha recensito \"${activity.songTitle ?: "una canzone"}\" di ${activity.artistName ?: "un artista"}"

                        else -> return@mapNotNull null
                    }

                    ActivityItem(message, activity.timestamp)
                }.sortedByDescending { it.timestamp }

                _currentUser.postValue(
                    UserWithData(
                        user = user,
                        activities = activities,
                        reviews = reviews,
                        friendsActivities = friendsActivities,
                        following = following,
                        followers = followers
                    )
                )
            }
            .addOnFailureListener {
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

    //Recupero dati profilo essenziali dell'Utente
    fun loadUserProfile(
        userId: String,
        onSuccess: (UserProfileData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("User").document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val username = document.getString("username") ?: "Unknown"
                val likes = document.getLong("likes") ?: 0L

                val followingRef = userDoc.collection("followingList")
                val followersRef = userDoc.collection("followersList")

                followingRef.get().addOnSuccessListener { followingDocs ->
                    val followingCount = followingDocs.size()

                    followersRef.get().addOnSuccessListener { followersDocs ->
                        val followersCount = followersDocs.size()

                        onSuccess(
                            UserProfileData(
                                username = username,
                                likes = likes,
                                followersCount = followersCount,
                                followingCount = followingCount
                            )
                        )
                    }.addOnFailureListener(onFailure)
                }.addOnFailureListener(onFailure)
            } else {
                onFailure(Exception("Documento utente non trovato"))
            }
        }.addOnFailureListener(onFailure)
    }

}
