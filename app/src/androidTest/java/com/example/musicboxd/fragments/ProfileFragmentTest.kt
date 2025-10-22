package com.example.musicboxd.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<ProfileFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Per viste il cui contenuto può arrivare dopo (Title, likes...), usiamo withEffectiveVisibility
        onView(withId(R.id.Title)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.likes)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.reviews)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.playlist)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.settings)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.network)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun clicking_reviews_navigates_toShowReviews() {
        val scenario = launchFragmentInContainer<ProfileFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        onView(withId(R.id.reviews)).perform(click())
        verify(nav).navigate(R.id.action_profileFragment_to_showReviews)
    }

    @Test
    fun clicking_settings_navigates_toSettings() {
        val scenario = launchFragmentInContainer<ProfileFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        onView(withId(R.id.settings)).perform(click())
        verify(nav).navigate(R.id.action_profileFragment_to_settingsFragment)
    }

    @Test
    fun clicking_network_navigates_toNetwork() {
        val scenario = launchFragmentInContainer<ProfileFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        onView(withId(R.id.network)).perform(click())
        verify(nav).navigate(R.id.action_profileFragment_to_network)
    }

    @Test
    fun clicking_playlist_navigates_toPlaylist() {
        val scenario = launchFragmentInContainer<ProfileFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        onView(withId(R.id.playlist)).perform(click())
        verify(nav).navigate(R.id.action_profileFragment_to_playlist)
    }
}
