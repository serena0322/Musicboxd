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
import com.example.musicboxd.adapter.TrackAdapter
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowUserPlaylistTest {

    private fun argsBundle(
        playlistName: String = "Playlist di Test",
        userId: String = "user_123"
    ) = Bundle().apply {
        // Chiavi generate da Safe Args (corrispondono ai nomi dei parametri in *ShowUserPlaylistArgs*)
        putString("playlistName", playlistName)
        putString("userId", userId)
    }

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<ShowUserPlaylist>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Usiamo withEffectiveVisibility per evitare problemi di width=0 in assenza dati backend
        onView(withId(R.id.name_playlist))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        onView(withId(R.id.RecyclerView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun name_isSet_fromArgs() {
        val name = "Preferiti di Luca"
        launchFragmentInContainer<ShowUserPlaylist>(
            fragmentArgs = argsBundle(playlistName = name),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.name_playlist)).check(matches(withText(name)))
    }

    @Test
    fun recycler_hasLinearLayoutManager_andTrackAdapter() {
        val scenario = launchFragmentInContainer<ShowUserPlaylist>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.RecyclerView)

            // LayoutManager corretto
            assert(rv.layoutManager is LinearLayoutManager) {
                "LayoutManager atteso: LinearLayoutManager, trovato: ${rv.layoutManager}"
            }

            // Adapter corretto
            assertThat(rv.adapter, instanceOf(TrackAdapter::class.java))
        }
    }
}
