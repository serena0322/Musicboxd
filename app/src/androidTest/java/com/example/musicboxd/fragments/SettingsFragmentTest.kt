package com.example.musicboxd.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    @Test
    fun coreViews_areVisible() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.username)).check(matches(isDisplayed()))
        onView(withId(R.id.firstName)).check(matches(isDisplayed()))
        onView(withId(R.id.lastName)).check(matches(isDisplayed()))
        onView(withId(R.id.email)).check(matches(isDisplayed()))

        onView(withId(R.id.signOut)).check(matches(isDisplayed()))
        onView(withId(R.id.cancel)).check(matches(isDisplayed()))
        onView(withId(R.id.security)).check(matches(isDisplayed()))
    }

    @Test
    fun tapping_username_opensInputDialog_thenCancel_closes() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // apre dialog di input
        onView(withId(R.id.username)).perform(click())

        // dialog visibile con EditText e bottoni
        onView(withText("Insert your username")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))

        // chiudi con Annulla e verifica non esista più
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())
        onView(withText("Insert your username")).check(doesNotExist())
    }

    @Test
    fun tapping_firstName_opensDialog_showsButtons() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.firstName)).perform(click())
        onView(withText("Insert your name")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())
        onView(withText("Insert your name")).check(doesNotExist())
    }

    @Test
    fun tapping_lastName_opensDialog_showsButtons() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.lastName)).perform(click())
        onView(withText("Insert your last name")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())
        onView(withText("Insert your last name")).check(doesNotExist())
    }

    @Test
    fun tapping_signOut_showsConfirmDialog_andCancel_dismisses() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.signOut)).perform(click())

        onView(withText("Esci")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Sì")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))

        // non confermiamo per evitare side–effects; chiudiamo
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())
        onView(withText("Esci")).check(doesNotExist())
    }

    @Test
    fun tapping_cancelAccount_showsConfirmDialog_andCancel_dismisses() {
        launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.cancel)).perform(click())

        onView(withText("Elimina account")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Sì")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))

        onView(withText("Annulla")).inRoot(isDialog()).perform(click())
        onView(withText("Elimina account")).check(doesNotExist())
    }

    @Test
    fun tapping_security_navigates_toPasswordAndAuthentication() {
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        onView(withId(R.id.security)).perform(click())

        verify(nav).navigate(R.id.action_settingsFragment_to_PasswordandAuthentication)
        verifyNoMoreInteractions(nav)
    }
}
