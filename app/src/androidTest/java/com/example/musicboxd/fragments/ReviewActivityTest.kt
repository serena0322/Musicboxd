package com.example.musicboxd.fragments

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import com.example.musicboxd.network.Track
import com.example.musicboxd.network.Artist
import com.example.musicboxd.network.Album
import org.hamcrest.CoreMatchers.not
import org.hamcrest.text.IsEmptyString.isEmptyString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewActivityTest {

    @Test
    fun launch_withTrack_populatesHeader() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Usa SOLO i modelli del package network
        val track = Track(
            id = 123L,
            title = "Test Song",
            artist = Artist(name = "Test Artist"),
            album = Album(
                id = 20L,
                title = "Test Album",
                genres = null,
                cover = "https://example.com/cover.jpg",
                releaseDate = "2024-10-21"
            ),
            duration = 210,
            preview = "https://example.com/preview.mp3"
        )


        val intent = Intent(ctx, ReviewActivity::class.java).apply {
            putExtra("track", track)
        }

        ActivityScenario.launch<ReviewActivity>(intent)

        onView(withId(R.id.title))
            .check(matches(isDisplayed()))
            .check(matches(withText("Test Song")))

        onView(withId(R.id.artist))
            .check(matches(isDisplayed()))
            .check(matches(withText("Test Artist")))

        onView(withId(R.id.textView4))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(isEmptyString()))))

        onView(withId(R.id.coverImage)).check(matches(isDisplayed()))
        onView(withId(R.id.save)).check(matches(isDisplayed()))
    }

    @Test
    fun launch_withoutTrack_usesFallbacks() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(ctx, ReviewActivity::class.java)

        ActivityScenario.launch<ReviewActivity>(intent)

        onView(withId(R.id.title))
            .check(matches(isDisplayed()))
            .check(matches(withText("una canzone")))

        onView(withId(R.id.artist))
            .check(matches(isDisplayed()))
            .check(matches(withText("un artista")))

        onView(withId(R.id.textView4))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(isEmptyString()))))

        onView(withId(R.id.coverImage)).check(matches(isDisplayed()))
    }

    @Test
    fun coreViews_areVisible_onLaunch() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(ctx, ReviewActivity::class.java)

        ActivityScenario.launch<ReviewActivity>(intent)

        onView(withId(R.id.ratingBar)).check(matches(isDisplayed()))
        onView(withId(R.id.textReview)).check(matches(isDisplayed()))
        onView(withId(R.id.like)).check(matches(isDisplayed()))
        onView(withId(R.id.heartImage)).check(matches(isDisplayed()))
        onView(withId(R.id.rate)).check(matches(isDisplayed()))
        onView(withId(R.id.save)).check(matches(isDisplayed()))
    }
}
