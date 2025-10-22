package com.example.musicboxd

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SecondActivityTest {

    @Before
    fun setup() {
        Intents.init()

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.clearInstancesForTest()
        FirebaseApp.initializeApp(
            ctx,
            FirebaseOptions.Builder()
                .setProjectId("demo-test")
                .setApplicationId("1:1234567890:android:test")
                .setApiKey("fake-api-key")
                .build()
        )

        // >>> AGGIUNGI QUESTA RIGA (prima di qualunque getInstance()/login) <<<
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun teardown() {
        FirebaseAuth.getInstance().signOut()
        Intents.release()
    }

    // Helper: esegue login anonimo e attende che currentUser sia valorizzato
    private fun loginAnonBlocking(timeoutSec: Long = 15) {
        val auth = FirebaseAuth.getInstance()
        val latch = CountDownLatch(1)
        val listener = FirebaseAuth.AuthStateListener { fa ->
            if (fa.currentUser != null) latch.countDown()
        }
        auth.addAuthStateListener(listener)
        auth.signInAnonymously().addOnFailureListener { e ->
            throw AssertionError("signInAnonymously() fallita: ${e.message}", e)
        }
        if (!latch.await(timeoutSec, TimeUnit.SECONDS)) {
            auth.removeAuthStateListener(listener)
            throw AssertionError("Auth non pronta: currentUser è ancora null")
        }
        auth.removeAuthStateListener(listener)
    }

    @Test
    fun notLoggedIn_remainsOnSecondActivity() {
        val scenario = ActivityScenario.launch(SecondActivity::class.java)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Non deve partire MainActivity
        intended(hasComponent(MainActivity::class.java.name), Intents.times(0))
        scenario.close()
    }

    @Test
    fun loggedIn_navigatesToMainActivity() {
        // Intercetta la startActivity verso MainActivity per non aprirla davvero
        Intents.intending(hasComponent(MainActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        // Esegui un login reale PRIMA di lanciare l’Activity
        loginAnonBlocking()

        // Ora SecondActivity dovrebbe navigare subito a MainActivity
        val scenario = ActivityScenario.launch(SecondActivity::class.java)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        intended(hasComponent(MainActivity::class.java.name), Intents.times(1))
        scenario.close()
    }
}
