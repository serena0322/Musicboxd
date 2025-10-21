package com.example.musicboxd.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import com.example.musicboxd.testutil.ToastMatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    @Test
    fun views_areVisible() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.email)).check(matches(isDisplayed()))
        onView(withId(R.id.password)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.credentialRecovery)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewSignup)).check(matches(isDisplayed()))
    }

    @Test
    fun click_signup_navigates_to_signInFragment() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val navController = mock(NavController::class.java)

        scenario.onFragment { fragment ->
            // collega un NavController mockato al root view del fragment
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // tap su "Registrati"
        onView(withId(R.id.textViewSignup)).perform(click())

        // verifica che sia stata invocata la navigation giusta
        verify(navController).navigate(R.id.action_loginFragment_to_signInFragment)
    }

    @Test
    fun click_recover_emptyEmail_doesNotNavigate_andUiStays() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val navController = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Tap su "Recupero credenziali" con email vuota
        onView(withId(R.id.credentialRecovery)).perform(click())

        // La UI è ancora presente (nessuna chiusura/navigazione)
        onView(withId(R.id.email)).check(matches(isDisplayed()))
        onView(withId(R.id.password)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()))

        // Nessuna navigazione deve essere avvenuta
        verifyNoInteractions(navController)
    }

}
