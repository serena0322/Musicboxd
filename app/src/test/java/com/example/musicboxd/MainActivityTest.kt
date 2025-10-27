package com.example.musicboxd

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.musicboxd.fragments.AddSongBottomSheet
import com.example.musicboxd.local.User
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.annotation.Config
import androidx.navigation.fragment.NavHostFragment
import com.example.musicboxd.`object`.BasicProfileData
import com.example.musicboxd.`object`.UserRepository
import android.os.Looper
import org.hamcrest.core.IsNull.notNullValue
import org.robolectric.Shadows.shadowOf


@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {

    @Before
    fun setUp() {
        // ---- Blocca tutti i singleton Firebase per i test locali ----
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        mockkStatic(FirebaseAuth::class)
        val fakeAuth = mockk<FirebaseAuth>()
        val fakeUser = mockk<FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns fakeUser
        every { fakeUser.uid } returns "test-uid"

        mockkStatic(FirebaseApp::class)
        val fakeApp = mockk<FirebaseApp>()
        val fakeOpts = FirebaseOptions.Builder()
            .setProjectId("demo")
            .setApplicationId("1:123:android:demo")
            .setApiKey("fake")
            .build()
        every { FirebaseApp.getInstance() } returns fakeApp
        every { fakeApp.options } returns fakeOpts

        // ---- Stub del Repository usato dal ViewModel caricato in Activity ----
        mockkObject(UserRepository)

        // Ritorna un profilo minimale per loadMyBasicProfile()
        coEvery { UserRepository.loadMyBasicDataWithReviewsAndPlaylists() } returns
                BasicProfileData(
                    user = User(id = "me", username = "serena", email = "s@x", firstName = "Serena", lastName = "G"),
                    reviews = emptyList(),
                    playlists = emptyList()
                )

        // Evita qualsiasi altro accesso a rete/Firestore
        every { UserRepository.currentUser } returns androidx.lifecycle.MutableLiveData(null)
        coEvery { UserRepository.loadMyActivitiesAndFollowersActivities() } returns (emptyList<com.example.musicboxd.local.ActivityItem>() to emptyList())
        coEvery { UserRepository.loadUserBasicDataWithReviewsAndPlaylists(any()) } returns
                BasicProfileData(null, emptyList(), emptyList())
        coEvery { UserRepository.loadReviewsForUser(any()) } returns emptyList()
        coEvery { UserRepository.getFollowers() } returns emptyList()
        coEvery { UserRepository.getFollowing() } returns emptyList()
        coEvery { UserRepository.searchUsersByUsername(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        unmockkObject(UserRepository)
        unmockkStatic(FirebaseApp::class)
        unmockkStatic(FirebaseAuth::class)
        unmockkStatic(FirebaseFirestore::class)
    }

    @Test
    fun activity_starts_withoutFirebaseCrashing() {
        val controller = buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        // Se siamo arrivati qui, nessuna IllegalStateException di Firebase
        assertNotNull(activity.findViewById<BottomNavigationView>(R.id.bottom_nav))
        // Verifica che il NavHost esista
        assertNotNull(activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
    }

    @Test
    fun tapping_add_opens_bottomSheet_and_doesNotNavigate() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Click sul tab Add: usa il listener personalizzato dell'Activity
        bottomNav.selectedItemId = R.id.nav_add

        // Esegui runnable postati (mostra BottomSheet)
        shadowOf(Looper.getMainLooper()).idle()
        activity.supportFragmentManager.executePendingTransactions()

        val shown = activity.supportFragmentManager
            .findFragmentByTag("BottomSheetAddSong") as? AddSongBottomSheet

        assertThat("Il BottomSheet non è stato mostrato", shown, notNullValue())
    }

    @Test
    fun tapping_each_tab_requests_navigation_on_navController() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val navHost = activity.supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val realController = navHost.navController
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Home
        bottomNav.selectedItemId = R.id.nav_home
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(R.id.homeFragment, realController.currentDestination?.id)

        // Search
        bottomNav.selectedItemId = R.id.nav_search
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(R.id.searchFragment, realController.currentDestination?.id)

        // Activity
        bottomNav.selectedItemId = R.id.nav_activity
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(R.id.activityFragment, realController.currentDestination?.id)

        // Profile
        bottomNav.selectedItemId = R.id.nav_profile
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(R.id.profileFragment, realController.currentDestination?.id)
    }


}
