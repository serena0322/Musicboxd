package com.example.musicboxd.fragments

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class ShowSongPlaylistTest {

    private fun argsBundle(): Bundle = bundleOf(
        // chiave generata da SafeArgs -> "playlistId"
        "playlistId" to "playlist_test_id"
    )

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<ShowSongPlaylist>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.name_playlist))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        onView(withId(R.id.RecyclerView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun recycler_isLinear_andAdapterIsTrackAdapter() {
        val scenario = launchFragmentInContainer<ShowSongPlaylist>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.RecyclerView)
            assertTrue("LayoutManager deve essere LinearLayoutManager", rv.layoutManager is LinearLayoutManager, )
            assertTrue("Adapter deve essere TrackAdapter", rv.adapter is TrackAdapter, )
        }
    }
}
