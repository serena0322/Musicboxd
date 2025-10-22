package com.example.musicboxd.fragments

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicboxd.R
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.action.ViewActions.click
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class PasswordFragmentTest {

    @Test
    fun views_areVisible_andDialogOpens() {
        // Prepara una password salvata
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit().putString("saved_password", "abc123").commit()

        val scenario = launchFragmentInContainer<PasswordFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Verifica che il trigger "Cambia Password" sia visibile
        onView(withId(R.id.changePassword))
            .check(matches(isDisplayed()))
            .perform(click())

        // Il dialog deve essere visibile con i 3 campi
        onView(withId(R.id.editCurrentPassword))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(R.id.editNewPassword))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(R.id.editConfirmPassword))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        // Bottoni del dialog presenti
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Annulla")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun wrongCurrentPassword_keepsDialogOpen_noNavigation() {
        // saved_password = abc123
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit().putString("saved_password", "abc123").commit()

        val scenario = launchFragmentInContainer<PasswordFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        val nav = mock(NavController::class.java)
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), nav)
        }

        // Apri dialog
        onView(withId(R.id.changePassword)).perform(click())

        // Inserisci password attuale sbagliata + nuove coerenti
        onView(withId(R.id.editCurrentPassword))
            .inRoot(isDialog())
            .perform(replaceText("WRONG"), closeSoftKeyboard())
        onView(withId(R.id.editNewPassword))
            .inRoot(isDialog())
            .perform(replaceText("newpass1"), closeSoftKeyboard())
        onView(withId(R.id.editConfirmPassword))
            .inRoot(isDialog())
            .perform(replaceText("newpass1"), closeSoftKeyboard())

        // Premo OK
        onView(withText("OK")).inRoot(isDialog()).perform(click())

        // Il dialog rimane visibile (validazione fallita)
        onView(withId(R.id.editCurrentPassword))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        // Nessuna navigazione deve essere avvenuta
        verifyNoInteractions(nav)
    }

    @Test
    fun tooShortNewPassword_keepsDialogOpen() {
        // saved_password = abc123
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit().putString("saved_password", "abc123").commit()

        val scenario = launchFragmentInContainer<PasswordFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // Apri dialog
        onView(withId(R.id.changePassword)).perform(click())

        // Password attuale corretta ma nuova troppo corta (<6)
        onView(withId(R.id.editCurrentPassword))
            .inRoot(isDialog())
            .perform(replaceText("abc123"), closeSoftKeyboard())
        onView(withId(R.id.editNewPassword))
            .inRoot(isDialog())
            .perform(replaceText("123"), closeSoftKeyboard())
        onView(withId(R.id.editConfirmPassword))
            .inRoot(isDialog())
            .perform(replaceText("123"), closeSoftKeyboard())

        onView(withText("OK")).inRoot(isDialog()).perform(click())

        // Il dialog resta aperto
        onView(withId(R.id.editNewPassword))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    // helper di attesa (mettilo nella stessa classe di test, fuori dai @Test)
    private fun idleFor(millis: Long): ViewAction = object : ViewAction {
        override fun getDescription() = "Wait for $millis ms"
        override fun getConstraints() = isRoot()
        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }

    @Test
    fun cancelButton_closesDialog() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit().putString("saved_password", "abc123").commit()

        launchFragmentInContainer<PasswordFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // apre il dialog
        onView(withId(R.id.changePassword)).perform(click())
        onView(withId(R.id.editCurrentPassword)).inRoot(isDialog()).check(matches(isDisplayed()))

        // chiude con "Annulla"
        onView(withText("Annulla")).inRoot(isDialog()).perform(click())

        // piccola attesa per la chiusura
        onView(isRoot()).perform(idleFor(200))

        // ora la view del dialog NON deve più esistere
        onView(withId(R.id.editCurrentPassword)).check(doesNotExist())

        // opzionale: la view del fragment è visibile
        onView(withId(R.id.changePassword)).check(matches(isDisplayed()))
    }
}
