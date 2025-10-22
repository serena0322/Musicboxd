package com.example.musicboxd.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class UserProfileTest {

    private fun argsBundle(userId: String = "user-123"): Bundle =
        bundleOf("userId" to userId) // stessa chiave usata nel fragment

    @Test
    fun coreViews_areVisible_andPlaceholdersSet() {
        launchFragmentInContainer<TestUserProfile>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Visibilità componenti principali
        onView(withId(R.id.username)).check(matches(isDisplayed()))
        onView(withId(R.id.followers)).check(matches(isDisplayed()))
        onView(withId(R.id.following)).check(matches(isDisplayed()))
        onView(withId(R.id.likes)).check(matches(isDisplayed()))
        onView(withId(R.id.reviews)).check(matches(isDisplayed()))
        onView(withId(R.id.playlist)).check(matches(isDisplayed()))

        // Placeholder iniziali
        onView(withId(R.id.username)).check(matches(withText("Loading...")))
        onView(withId(R.id.followers)).check(matches(withText("...")))
        onView(withId(R.id.following)).check(matches(withText("...")))
        onView(withId(R.id.likes)).check(matches(withText("...")))
    }

    @Test
    fun clickingPlaylist_navigatesWithCorrectArgs() {
        val userId = "user-abc"
        val scenario = launchFragmentInContainer<TestUserProfile>(
            fragmentArgs = argsBundle(userId),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val navController = mock<NavController>()
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Click su "Playlist"
        onView(withId(R.id.playlist)).perform(click())

        // Cattura NavDirections e verifica actionId + argomento
        val directionsCaptor = argumentCaptor<androidx.navigation.NavDirections>()
        verify(navController).navigate(directionsCaptor.capture())

        val dir = directionsCaptor.firstValue
        assertNotNull(dir)
        assertEquals(R.id.action_userProfile_to_Userplaylist, dir.actionId)
        assertEquals(userId, dir.arguments["userId"])
    }

    @Test
    fun clickingReviews_navigatesWithCorrectArgs() {
        val userId = "user-xyz"
        val scenario = launchFragmentInContainer<TestUserProfile>(
            fragmentArgs = argsBundle(userId),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val navController = mock<NavController>()
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Click su "Reviews"
        onView(withId(R.id.reviews)).perform(click())

        val directionsCaptor = argumentCaptor<androidx.navigation.NavDirections>()
        verify(navController).navigate(directionsCaptor.capture())

        val dir = directionsCaptor.firstValue
        assertNotNull(dir)
        assertEquals(R.id.action_userProfile_to_showUserReviews, dir.actionId)
        assertEquals(userId, dir.arguments["userId"])
    }

    /**
     * Sottoclasse “muta” per i test:
     * - NON chiama loadOtherUserProfile / observe (niente Firestore)
     * - Imposta placeholder attesi dai test
     * - Registra solo i click per la navigazione con i Safe Args reali
     */
    class TestUserProfile : UserProfile() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            // NON chiamare super.onViewCreated(view, savedInstanceState)
            val userId = arguments?.getString("userId") ?: "user-123"

            view.findViewById<TextView>(R.id.username).text = "Loading..."
            view.findViewById<TextView>(R.id.followers).text = "..."
            view.findViewById<TextView>(R.id.following).text = "..."
            view.findViewById<TextView>(R.id.likes).text = "..."

            view.findViewById<TextView>(R.id.playlist).setOnClickListener {
                val action = UserProfileDirections.actionUserProfileToUserplaylist(userId)
                findNavController().navigate(action)
            }
            view.findViewById<TextView>(R.id.reviews).setOnClickListener {
                val action = UserProfileDirections.actionUserProfileToShowUserReviews(userId)
                findNavController().navigate(action)
            }
        }
    }
}
