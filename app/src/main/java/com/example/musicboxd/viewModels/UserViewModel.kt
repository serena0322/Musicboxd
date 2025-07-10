package com.example.musicboxd.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.Review
import com.example.musicboxd.`object`.BasicProfileData
import com.example.musicboxd.`object`.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _myActivities = MutableLiveData<List<ActivityItem>>()
    val myActivities: LiveData<List<ActivityItem>> = _myActivities

    private val _friendsActivities = MutableLiveData<List<ActivityItem>>()
    val friendsActivities: LiveData<List<ActivityItem>> = _friendsActivities

    private var userListener: ListenerRegistration? = null

    fun observeMyUserRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userListener?.remove()
        userListener = UserRepository.observeUserDocument(uid) { updatedUser ->
            _basicProfile.value = _basicProfile.value?.copy(user = updatedUser)
        }
    }

    private var reviewListener: ListenerRegistration? = null
    private var playlistListener: ListenerRegistration? = null

    fun observeMyProfileDataRealtime() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        observeMyUserRealtime()

        reviewListener?.remove()
        reviewListener = UserRepository.observeUserReviewsRealtime(uid) { reviews ->
            _basicProfile.postValue(
                _basicProfile.value?.copy(reviews = reviews)
            )
        }

        playlistListener?.remove()
        playlistListener = UserRepository.observeUserPlaylistsRealtime(uid) { playlists ->
            _basicProfile.postValue(
                _basicProfile.value?.copy(playlists = playlists)
            )
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
        // Osserva le attività dell’utente
        UserRepository.observeMyActivityRealtime {
            _myActivities.postValue(it)
        }

        // Ottiene la lista dei following e osserva le loro attività
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
}
