package com.example.musicboxd

import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecondActivityTest {

    @Before
    fun setUp() {
        mockkStatic(FirebaseAuth::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun when_userIsLoggedIn_navigatesToMainActivity_withClearTaskFlags() {
        // Arrange: utente loggato
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        val fakeUser = mockk<FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns fakeUser

        // Act
        val controller = Robolectric.buildActivity(SecondActivity::class.java)
            .create()   // onCreate -> setContentView + init auth
            .start()    // onStart -> should navigate
        val activity = controller.get()

        // Assert
        val shadow = Shadows.shadowOf(activity)
        val startedIntent = shadow.nextStartedActivity
        assertNotNull("Non ha avviato MainActivity pur con utente loggato", startedIntent)
        assertEquals(
            MainActivity::class.java.name,
            startedIntent.component?.className
        )
        // Verifica FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
        val expectedFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val maskedFlags = startedIntent.flags and expectedFlags
        assertEquals( "I flag dell'Intent non corrispondono", expectedFlags, maskedFlags)
    }

    @Test
    fun when_userIsNotLoggedIn_staysOnSecondActivity() {
        // Arrange: utente NON loggato
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns null

        // Act
        val controller = Robolectric.buildActivity(SecondActivity::class.java)
            .create()
            .start()
        val activity = controller.get()

        // Assert
        val shadow = Shadows.shadowOf(activity)
        val startedIntent = shadow.nextStartedActivity
        assertNull("Ha navigato nonostante currentUser == null", startedIntent )
    }
}
