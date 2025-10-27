package com.example.musicboxd.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.Review
import com.example.musicboxd.local.User
import com.example.musicboxd.`object`.BasicProfileData
import com.example.musicboxd.`object`.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel completo per profili e feed: gestisce LiveData di recensioni (realtime e one-shot),
// attività personali/amici, profilo base proprio/altrui, followers/following e ricerca utenti,
// orchestrando Firestore e coroutine (viewModelScope).

class UserViewModel : ViewModel() {

    // ----- Home (Tab 1): Reviews -----
    private val _homeReviews = MutableLiveData<List<Review>>(emptyList())
    val homeReviews: LiveData<List<Review>> = _homeReviews
    // Mappa uid -> username (per la UI)
    private val _usernames = MutableLiveData<Map<String, String>>(emptyMap())
    val usernames: LiveData<Map<String, String>> = _usernames
    private val firestore = FirebaseFirestore.getInstance()
    private var homeReviewsListener: ListenerRegistration? = null
    private var followingCache: Set<String> = emptySet()

    /** Carica la lista dei following; prima dalla collezione 'User' (singolare), poi 'Users' (fallback). */
    private suspend fun loadFollowingSet(uid: String): Set<String> {
        val docUser = firestore.collection("User").document(uid).get().await()
        if (docUser.exists()) {
            @Suppress("UNCHECKED_CAST")
            return (docUser.get("following") as? List<String>)?.toSet() ?: emptySet()
        }
        val docUsers = firestore.collection("Users").document(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return (docUsers.get("following") as? List<String>)?.toSet() ?: emptySet()
    }

    fun resolveUsernamesFor(uids: Collection<String>) {
        viewModelScope.launch {
            val uniq = uids.filter { it.isNotBlank() }.toSet()
            if (uniq.isEmpty()) {
                _usernames.postValue(emptyMap())
                return@launch
            }

            val result = mutableMapOf<String, String>()
            // Firestore whereIn accetta max 10 id per volta
            for (chunk in uniq.chunked(10)) {
                try {
                    val snap = FirebaseFirestore.getInstance()
                        .collection("User") // se la tua collezione profili è "Users", cambia qui
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    for (doc in snap.documents) {
                        val name = doc.getString("username")
                            ?: doc.getString("displayName")
                            ?: doc.id
                        result[doc.id] = name
                    }
                } catch (_: Exception) { /* ignora eventuali chunk falliti */ }
            }
            _usernames.postValue(result)
        }
    }

    // Realtime senza orderBy: ordino lato client
    fun observeHomeReviewsRealtime() {
        if (homeReviewsListener != null) return
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            followingCache = if (uid != null) loadFollowingSet(uid) else emptySet()

            val q = firestore.collectionGroup("Reviews")  // <-- niente orderBy
                .limit(200)

            homeReviewsListener = q.addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    _homeReviews.postValue(emptyList()); return@addSnapshotListener
                }

                val mapped = snap.documents
                    .mapNotNull { it.toLocalReview() }
                    .sortedByDescending { it.timestamp?.toDate() } // <-- ordinamento client

                val (fromFollowed, fromOthers) = mapped.partition { r ->
                    r.sourceUserId.isNotBlank() && followingCache.contains(r.sourceUserId)
                }
                _homeReviews.postValue((fromFollowed.take(10) + fromOthers.take(20)).shuffled())
            }
        }
    }

    // One-shot senza orderBy: ordino lato client
    fun reloadHomeReviewsOnce() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (followingCache.isEmpty() && uid != null) followingCache = loadFollowingSet(uid)

                val snap = firestore.collectionGroup("Reviews")  // <-- niente orderBy
                    .limit(200)
                    .get().await()

                val mapped = snap.documents
                    .mapNotNull { it.toLocalReview() }
                    .sortedByDescending { it.timestamp?.toDate() } // <-- ordinamento client

                val (fromFollowed, fromOthers) = mapped.partition { r ->
                    r.sourceUserId.isNotBlank() && followingCache.contains(r.sourceUserId)
                }
                _homeReviews.postValue((fromFollowed.take(10) + fromOthers.take(20)).shuffled())
            } catch (_: Exception) {
                _homeReviews.postValue(emptyList())
            }
        }
    }


    /** Mapping aderente al tuo data class; autore ricavato dal path User/{uid}/Reviews/{doc}. */
    private fun DocumentSnapshot.toLocalReview(): Review? = try {
        val r = this.toObject<Review>() ?: return null
        r.documentId = this.id
        val authorId = this.reference.parent.parent?.id ?: ""
        if (r.sourceUserId.isBlank()) r.sourceUserId = this.getString("userId") ?: authorId
        when (val raw = this.get("rating")) {
            is Number -> r.rating = raw.toDouble()
            is String -> r.rating = raw.toDoubleOrNull() ?: r.rating
        }
        r.timestamp = this.getTimestamp("timestamp") ?: r.timestamp
        r
    } catch (_: Exception) { null }

    override fun onCleared() {
        super.onCleared()
        homeReviewsListener?.remove()
        homeReviewsListener = null
    }

    // ----- Resto del tuo ViewModel (activities, profili, ecc.) -----

    private val _myActivities = MutableLiveData<List<ActivityItem>>()
    val myActivities: LiveData<List<ActivityItem>> = _myActivities

    private val _friendsActivities = MutableLiveData<List<ActivityItem>>()
    val friendsActivities: LiveData<List<ActivityItem>> = _friendsActivities

    private var userListener: ListenerRegistration? = null
    private var reviewListener: ListenerRegistration? = null
    private var playlistListener: ListenerRegistration? = null

    fun observeMyUserRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userListener?.remove()
        userListener = UserRepository.observeUserDocument(uid) { updatedUser ->
            _basicProfile.value = _basicProfile.value?.copy(user = updatedUser)
        }
    }

    fun observeMyProfileDataRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        observeMyUserRealtime()
        reviewListener?.remove()
        reviewListener = UserRepository.observeUserReviewsRealtime(uid) { reviews ->
            _basicProfile.postValue(_basicProfile.value?.copy(reviews = reviews))
        }
        playlistListener?.remove()
        playlistListener = UserRepository.observeUserPlaylistsRealtime(uid) { playlists ->
            _basicProfile.postValue(_basicProfile.value?.copy(playlists = playlists))
        }
    }

    private val _basicProfile = MutableLiveData<BasicProfileData>()
    val basicProfile: LiveData<BasicProfileData> = _basicProfile
    private var isBasicProfileLoaded = false

    fun loadMyBasicProfile(forceReload: Boolean = false) {
        if (isBasicProfileLoaded && !forceReload) return
        viewModelScope.launch {
            val profileData = UserRepository.loadMyBasicDataWithReviewsAndPlaylists()
            _basicProfile.postValue(profileData)
            isBasicProfileLoaded = true
        }
    }

    private val _otherUserProfile = MutableLiveData<BasicProfileData>()
    val otherUserProfile: LiveData<BasicProfileData> = _otherUserProfile

    fun loadOtherUserProfile(userId: String) {
        viewModelScope.launch {
            val data = UserRepository.loadUserBasicDataWithReviewsAndPlaylists(userId)
            _otherUserProfile.postValue(data)
        }
    }

    fun loadMyAndFriendsActivities() {
        viewModelScope.launch {
            val (my, friends) = UserRepository.loadMyActivitiesAndFollowersActivities()
            _myActivities.postValue(my)
            _friendsActivities.postValue(friends)
        }
    }

    fun observeAllActivitiesRealtime() {
        UserRepository.observeMyActivityRealtime { _myActivities.postValue(it) }
        val followingList = UserRepository.currentUser.value?.following ?: emptyList()
        UserRepository.observeFriendsActivitiesRealtime(followingList) {
            _friendsActivities.postValue(it)
        }
    }

    private val _otherUserReviews = MutableLiveData<List<Review>>()
    val otherUserReviews: LiveData<List<Review>> = _otherUserReviews

    fun loadReviewsForUser(userId: String) {
        viewModelScope.launch {
            val reviews = UserRepository.loadReviewsForUser(userId)
            _otherUserReviews.postValue(reviews.sortedByDescending { it.timestamp })
        }
    }

    private val _followers = MutableLiveData<List<User>>()
    val followers: LiveData<List<User>> = _followers

    private val _following = MutableLiveData<List<User>>()
    val following: LiveData<List<User>> = _following

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    fun loadFollowers() = viewModelScope.launch {
        _followers.postValue(UserRepository.getFollowers())
    }

    fun loadFollowing() = viewModelScope.launch {
        _following.postValue(UserRepository.getFollowing())
    }

    fun performUserSearch(query: String) = viewModelScope.launch {
        _searchResults.postValue(UserRepository.searchUsersByUsername(query))
    }
}
