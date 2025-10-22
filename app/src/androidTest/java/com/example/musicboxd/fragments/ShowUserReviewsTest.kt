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
import com.example.musicboxd.adapter.ReviewAdapter
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowUserReviewsTest {

    private fun argsBundle(userId: String = "user_123") = Bundle().apply {
        // chiave generata da Safe Args: deve combaciare con il nome in ShowUserReviewsArgs
        putString("userId", userId)
    }

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<ShowUserReviews>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Verifica che il recycler sia visibile
        onView(withId(R.id.recyclerView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun recycler_hasLinearLayoutManager_andReviewAdapter() {
        val scenario = launchFragmentInContainer<ShowUserReviews>(
            fragmentArgs = argsBundle(),
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.recyclerView)

            // LayoutManager corretto
            assert(rv.layoutManager is LinearLayoutManager) {
                "Atteso LinearLayoutManager, trovato: ${rv.layoutManager}"
            }

            // Adapter corretto
            org.junit.Assert.assertThat(rv.adapter, instanceOf(ReviewAdapter::class.java))
        }
    }
}
