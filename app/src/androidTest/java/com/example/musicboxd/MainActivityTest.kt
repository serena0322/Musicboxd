package com.example.musicboxd

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Dichiara che la classe deve usare il runner AndroidJUnit4
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun bottomNav_homeIcon_navigatesToHomeFragment() {
        onView(withId(R.id.nav_home))
            .perform(click())

        onView(withId(R.id.home_root_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_addIcon_showsAddSongBottomSheet() {
        onView(withId(R.id.nav_add))
            .perform(click())

        onView(withId(R.id.bottomSheetContainer))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_searchIcon_navigatesToSearchFragment() {
        onView(withId(R.id.nav_search))
            .perform(click())

        onView(withId(R.id.search_root_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_homeIcon_navigatesToActivityFragment() {
        onView(withId(R.id.nav_activity))
            .perform(click())

        onView(withId(R.id.activity_root_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_homeIcon_navigatesToProfile() {
        onView(withId(R.id.nav_profile))
            .perform(click())

        onView(withId(R.id.profile_root_layout))
            .check(matches(isDisplayed()))
    }

}