package com.example.musicboxd.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.musicboxd.`object`.BasicProfileData
import com.example.musicboxd.`object`.UserRepository
import com.example.musicboxd.`object`.UserWithData
import com.example.musicboxd.local.*
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class UserViewModelTest {

    @get:Rule val instant = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule val main = com.example.musicboxd.testing.MainDispatcherRule()

    private lateinit var vm: UserViewModel

    private fun ts(ms: Long) = Timestamp(java.util.Date(ms))
    private fun review(id: String, userId: String, title: String, artist: String, t: Long) =
        Review(id, "review", artist, title, userId, "", 4.0, "ok", ts(t))
    private fun playlist(id: String, name: String, t: Long) =
        PlaylistItem(id, name, "u", ts(t), emptyList())

    @Before
    fun setUp() {
        // Mock del repo (come già fai)
        mockkObject(UserRepository)
        every { UserRepository.currentUser } returns MutableLiveData<UserWithData?>(null)

        // --- blocca Firebase in locale ---
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        mockkStatic(FirebaseAuth::class)
        val auth = mockk<FirebaseAuth>()
        val user = mockk<FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns auth
        every { auth.currentUser } returns user
        every { user.uid } returns "fake-uid"

        vm = UserViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(UserRepository)
        unmockkStatic(FirebaseFirestore::class)
        unmockkStatic(FirebaseAuth::class)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun loadMyBasicProfile_updatesLiveData_and_cachesCall() = runTest {
        val u = User(id="me", username="serena", email="serena@example.com",
            firstName="Serena", lastName="Giuliani", likes=0, followers=0, following=0)
        val data = BasicProfileData(u, listOf(review("r1","me","Fix You","Coldplay",1000)), listOf(playlist("p1","Preferite",2000)))
        coEvery { UserRepository.loadMyBasicDataWithReviewsAndPlaylists() } returns data

        vm.loadMyBasicProfile(); advanceUntilIdle()
        Assert.assertEquals("serena", vm.basicProfile.value?.user?.username)

        vm.loadMyBasicProfile(forceReload=false); advanceUntilIdle()
        coVerify(exactly = 1) { UserRepository.loadMyBasicDataWithReviewsAndPlaylists() }

        vm.loadMyBasicProfile(forceReload=true); advanceUntilIdle()
        coVerify(exactly = 2) { UserRepository.loadMyBasicDataWithReviewsAndPlaylists() }
    }


// ---------------------------
    // loadOtherUserProfile
    // ---------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadOtherUserProfile_postsData() = runTest {
        val u = User(
            id = "me",
            username = "serena",
            email = "serena@example.com",
            firstName = "Serena",
            lastName = "Giuliani",
            likes = 0,
            followers = 0,
            following = 0
        )
        val data = BasicProfileData(user = u, reviews = emptyList(), playlists = emptyList())
        coEvery { UserRepository.loadUserBasicDataWithReviewsAndPlaylists("u2") } returns data

        vm.loadOtherUserProfile("u2")
        advanceUntilIdle()

        Assert.assertEquals("serena", vm.otherUserProfile.value?.user?.username)
        coVerify { UserRepository.loadUserBasicDataWithReviewsAndPlaylists("u2") }
    }

    // ---------------------------
    // loadMyAndFriendsActivities
    // ---------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadMyAndFriendsActivities_updatesLiveData() = runTest {
        val my = listOf(ActivityItem("A", ts(10)), ActivityItem("B", ts(20)))
        val friends = listOf(ActivityItem("C", ts(30)))
        coEvery { UserRepository.loadMyActivitiesAndFollowersActivities() } returns (my to friends)

        vm.loadMyAndFriendsActivities()
        advanceUntilIdle()

        Assert.assertEquals(2, vm.myActivities.value?.size)
        Assert.assertEquals(1, vm.friendsActivities.value?.size)
    }

    // ---------------------------
    // observeAllActivitiesRealtime
    // ---------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun observeAllActivitiesRealtime_wiresCallbacks_and_receivesUpdates() = runTest {
        val mySlot = slot<(List<ActivityItem>) -> Unit>()
        val friendsSlot = slot<(List<ActivityItem>) -> Unit>()

        every { UserRepository.observeMyActivityRealtime(capture(mySlot)) } answers { }
        every { UserRepository.observeFriendsActivitiesRealtime(any(), capture(friendsSlot)) } answers { }

        val current = MutableLiveData<UserWithData?>(
            UserWithData(
                user = User(
                    id = "me",
                    username = "serena",
                    email = "serena@example.com",
                    firstName = "Serena",
                    lastName = "Giuliani",
                    likes = 0,
                    followers = 0,
                    following = 0
                ),
                activities = emptyList(),
                reviews = emptyList(),
                friendsActivities = emptyList(),
                following = emptyList(),
                followers = emptyList()
            )
        )
        every { UserRepository.currentUser } returns current

        vm.observeAllActivitiesRealtime()
        advanceUntilIdle()

        mySlot.captured.invoke(listOf(ActivityItem("X", ts(1))))
        friendsSlot.captured.invoke(listOf(ActivityItem("Y", ts(2))))

        Assert.assertEquals(1, vm.myActivities.value?.size)
        Assert.assertEquals("X", vm.myActivities.value?.first()?.content)
        Assert.assertEquals(1, vm.friendsActivities.value?.size)
        Assert.assertEquals("Y", vm.friendsActivities.value?.first()?.content)
    }

    // ---------------------------
    // loadReviewsForUser
    // ---------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReviewsForUser_postsSortedDescending() = runTest {
        val rOld = review("r1", "u", "Song1", "A", 1000)
        val rNew = review("r2", "u", "Song2", "B", 5000)
        coEvery { UserRepository.loadReviewsForUser("u") } returns listOf(rOld, rNew)

        vm.loadReviewsForUser("u")
        advanceUntilIdle()

        val list = vm.otherUserReviews.value!!
        Assert.assertEquals(2, list.size)
        Assert.assertEquals("r2", list.first().documentId)
    }

    // ---------------------------
    // loadFollowers / loadFollowing / performUserSearch
    // ---------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadsFollowersFollowingAndSearch_resultsPosted() = runTest {
        val u1 = User(id = "a", username = "a", email = "a@a.it", firstName = "A", lastName = "A")
        val u2 = User(id = "b", username = "b", email = "b@b.it", firstName = "B", lastName = "B")

        coEvery { UserRepository.getFollowers() } returns listOf(u1)
        coEvery { UserRepository.getFollowing() } returns listOf(u2)
        coEvery { UserRepository.searchUsersByUsername("se") } returns listOf(u1, u2)

        vm.loadFollowers(); vm.loadFollowing(); vm.performUserSearch("se")
        advanceUntilIdle()

        Assert.assertEquals(listOf(u1), vm.followers.value)
        Assert.assertEquals(listOf(u2), vm.following.value)
        Assert.assertEquals(listOf(u1, u2), vm.searchResults.value)
    }
}
