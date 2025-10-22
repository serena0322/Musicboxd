package com.example.musicboxd.fragments

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPlaylistsTest {

    private fun argsBundle(userId: String = "test-user-123") = Bundle().apply {
        putString("userId", userId) // Safe Args genera anche la chiave; qui usiamo quella plain
    }

    @Test
    fun coreViews_areVisible_andCreateHidden() {
        launchFragmentInContainer<UserPlaylists>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Recycler presente e visibile
        onView(withId(R.id.activityRecyclerView)).check(matches(isDisplayed()))
        // Bottone "crea" nascosto in questa schermata (è una TextView con id button)
        onView(withId(R.id.button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun recycler_hasLayoutManager_andAdapterAttached() {
        val scenario = launchFragmentInContainer<UserPlaylists>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.activityRecyclerView)
            // LayoutManager corretto
            assertTrue("LayoutManager non è LinearLayoutManager", rv.layoutManager is LinearLayoutManager)
            // Adapter collegato
            assertTrue("Adapter non è stato impostato", rv.adapter != null)
        }
    }

    @Test
    fun recycler_initialItemCount_isZero_beforeFirestoreCallback() {
        val scenario = launchFragmentInContainer<UserPlaylists>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.activityRecyclerView)
            // Prima che arrivi lo snapshot listener reale, la lista è vuota
            val count = rv.adapter?.itemCount ?: -1
            assertTrue("ItemCount iniziale atteso 0, trovato $count", count == 0)
        }
    }
}
