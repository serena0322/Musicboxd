package com.example.musicboxd

import android.app.Activity
import android.content.Context
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents // DEVE ESSERE RICONOSCIUTO
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.* // DEVE ESSERE RICONOSCIUTO
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SecondActivityTest {

    @Before
    fun setup() {
        Intents.init()

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.clearInstancesForTest()
        val opts = FirebaseOptions.Builder()
            .setProjectId("demo-test")
            .setApplicationId("1:1234567890:android:test")
            .setApiKey("fake-api-key")
            .build()
        FirebaseApp.initializeApp(ctx, opts)

        FirebaseAuth.getInstance().apply {
            useEmulator("10.0.2.2", 9099) // <-- importante: NON 127.0.0.1
            signOut()
        }
    }


    @After
    fun teardown() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        Intents.release()
    }

    @Test
    fun notLoggedIn_remainsOnSecondActivity() {
        val scenario = androidx.test.core.app.ActivityScenario.launch(SecondActivity::class.java)
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        intended(hasComponent(MainActivity::class.java.name), Intents.times(0))
        scenario.close()
    }

    @Test
    fun loggedIn_navigatesToMainActivity() {
        Intents.intending(hasComponent(MainActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        val auth = FirebaseAuth.getInstance()
        val latch = java.util.concurrent.CountDownLatch(1)
        val listener = FirebaseAuth.AuthStateListener { fa ->
            if (fa.currentUser != null) latch.countDown()
        }
        auth.addAuthStateListener(listener)
        auth.signInAnonymously().addOnFailureListener { e ->
            throw AssertionError("signInAnonymously() fallita: ${e.message}", e)
        }
        if (!latch.await(15, java.util.concurrent.TimeUnit.SECONDS)) {
            auth.removeAuthStateListener(listener)
            throw AssertionError("Auth non pronta: currentUser è ancora null (emulatore su 9099 avviato?)")
        }
        auth.removeAuthStateListener(listener)

        val scenario = ActivityScenario.launch(SecondActivity::class.java)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        intended(hasComponent(MainActivity::class.java.name), Intents.times(1))
        scenario.close()
    }

}
