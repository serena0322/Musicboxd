package com.example.musicboxd.fragments

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import com.example.musicboxd.adapter.PlaylistAdapter
import com.google.android.material.R as MaterialR
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PlaylistTest {

    @Test
    fun fragmentInflates_coreViewsPresent() {
        val scenario = launchFragmentInContainer<Playlist>(
            themeResId = MaterialR.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val rv = root.findViewById<RecyclerView>(R.id.activityRecyclerView)
            val btn = root.findViewById<View>(R.id.button)

            assertTrue("RecyclerView mancante", rv != null)
            assertTrue("Pulsante mancante", btn != null)
            assertTrue("LayoutManager non è LinearLayoutManager", rv.layoutManager is LinearLayoutManager)
            assertTrue("Adapter non è PlaylistAdapter", rv.adapter is PlaylistAdapter)
        }
    }

    @Test
    fun clickNewPlaylist_showsCreateDialog() {
        launchFragmentInContainer<Playlist>(
            themeResId = MaterialR.style.Theme_Material3_DayNight_NoActionBar
        )

        // Click sul pulsante "Nuova Playlist"
        onView(withId(R.id.button)).check(matches(isDisplayed())).perform(click())

        // Verifica che il dialog sia visibile (titolo + bottoni)
        onView(withText("Nuova Playlist")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Crea")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun clickCancel_onCreateDialog_closesDialog() {
        launchFragmentInContainer<Playlist>(
            themeResId = MaterialR.style.Theme_Material3_DayNight_NoActionBar
        )

        // Apre il dialog
        onView(withId(R.id.button)).perform(click())
        onView(withText("Nuova Playlist")).inRoot(isDialog()).check(matches(isDisplayed()))

        // Click su "Annulla"
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())

        // Attendi brevemente la chiusura
        onView(isRoot()).perform(idleFor(200))

        // Il dialog non deve più esistere
        onView(withText("Nuova Playlist")).check(doesNotExist())
    }

    // Helper di attesa "pulita" per evitare race conditions
    private fun idleFor(millis: Long): ViewAction = object : ViewAction {
        override fun getDescription() = "wait for $millis ms"
        override fun getConstraints() = isRoot()
        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }
}
