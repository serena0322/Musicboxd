package com.example.musicboxd.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class SignInFragmentTest {

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<SignInFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.email))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.username))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.password))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.buttonSignIn))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(isClickable()))
    }

    @Test
    fun inputFields_acceptText() {
        launchFragmentInContainer<SignInFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.email))
            .perform(typeText("test@example.com"), closeSoftKeyboard())
            .check(matches(withText("test@example.com")))

        onView(withId(R.id.username))
            .perform(typeText("testuser"), closeSoftKeyboard())
            .check(matches(withText("testuser")))

        onView(withId(R.id.password))
            .perform(typeText("passw0rd"), closeSoftKeyboard())
            .check(matches(withText("passw0rd")))
    }

    @Test
    fun clickRegister_withEmptyFields_doesNotNavigate() {
        val scenario = launchFragmentInContainer<SignInFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val navController = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Campi vuoti -> preme “Register”
        onView(withId(R.id.buttonSignIn)).perform(click())

        // Nessuna navigazione deve avvenire
        verifyNoInteractions(navController)
    }
}
