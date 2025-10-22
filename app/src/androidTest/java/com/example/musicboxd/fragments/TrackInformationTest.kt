package com.example.musicboxd.fragments

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import com.example.musicboxd.network.Album
import com.example.musicboxd.network.Artist
import com.example.musicboxd.network.Track
import org.hamcrest.CoreMatchers.not
import org.hamcrest.text.IsEmptyString.isEmptyString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackInformationTest {

    private fun buildTrack(
        withPreview: Boolean = false
    ) = Track(
        id = 123L,
        title = "Test Song",
        artist = Artist(name = "Test Artist"),
        album = Album(
            id = 999L,
            title = "Test Album",
            genres = null,
            cover = "https://example.com/cover.jpg",
            releaseDate = "2020-01-15"
        ),
        duration = 185,
        preview = if (withPreview) "https://example.com/prev.mp3" else null
    )

    @Test
    fun coreViews_areVisible_onLaunch_withoutExtras() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(ctx, TrackInformation::class.java)

        ActivityScenario.launch<TrackInformation>(intent)

        onView(withId(R.id.infoCover)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.artist)).check(matches(isDisplayed()))
        onView(withId(R.id.album)).check(matches(isDisplayed()))
        onView(withId(R.id.duration)).check(matches(isDisplayed()))
        onView(withId(R.id.rating)).check(matches(isDisplayed()))
        onView(withId(R.id.play)).check(matches(isDisplayed()))
        onView(withId(R.id.playlist)).check(matches(isDisplayed()))
    }

    @Test
    fun launch_withTrack_populatesHeaderFields() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val track = buildTrack(withPreview = false)

        val intent = Intent(ctx, TrackInformation::class.java).apply {
            putExtra("track", track)
            putExtra("cover", track.album?.cover) // l’Activity legge "cover" dall’Intent
        }

        ActivityScenario.launch<TrackInformation>(intent)

        onView(withId(R.id.title))
            .check(matches(isDisplayed()))
            .check(matches(withText("Test Song")))

        onView(withId(R.id.artist))
            .check(matches(isDisplayed()))
            .check(matches(withText("Test Artist")))

        onView(withId(R.id.album))
            .check(matches(isDisplayed()))
            .check(matches(withText("Test Album")))

        // La durata è formattata (non vuota)
        onView(withId(R.id.duration))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(isEmptyString()))))
    }

    @Test
    fun formatHelpers_work_asExpected() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val scenario = ActivityScenario.launch<TrackInformation>(Intent(ctx, TrackInformation::class.java))

        scenario.onActivity { act ->
            // formatDuration
            org.junit.Assert.assertEquals("3:05 min", act.formatDuration(185))
            org.junit.Assert.assertEquals("Durata sconosciuta", act.formatDuration(null))

            // formatDate
            org.junit.Assert.assertEquals("15 gennaio 2020", act.formatDate("2020-01-15"))
            org.junit.Assert.assertEquals("Data sconosciuta", act.formatDate(null))
            org.junit.Assert.assertEquals("Data sconosciuta", act.formatDate("invalid"))
        }
    }

    @Test
    fun clickPreview_withoutUrl_doesNotCrash_andButtonRemainsVisible() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val track = buildTrack(withPreview = false)

        val intent = Intent(ctx, TrackInformation::class.java).apply {
            putExtra("track", track)
            putExtra("cover", track.album?.cover)
        }

        ActivityScenario.launch<TrackInformation>(intent)

        // Clic sul bottone play: senza preview non deve crashare; verifichiamo che resti visibile
        onView(withId(R.id.play)).perform(click())
        onView(withId(R.id.play)).check(matches(isDisplayed()))
    }

    @Test
    fun launch_withTrack_showsCoverAndButtons() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val track = buildTrack(withPreview = true)

        val intent = Intent(ctx, TrackInformation::class.java).apply {
            putExtra("track", track)
            putExtra("cover", track.album?.cover)
        }

        ActivityScenario.launch<TrackInformation>(intent)

        onView(withId(R.id.infoCover)).check(matches(isDisplayed()))
        onView(withId(R.id.play)).check(matches(isDisplayed()))
        onView(withId(R.id.playlist)).check(matches(isDisplayed()))
    }
}
