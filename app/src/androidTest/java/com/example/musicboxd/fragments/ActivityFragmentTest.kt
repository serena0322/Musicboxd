package com.example.musicboxd.fragments

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicboxd.R
import com.google.android.material.tabs.TabLayout
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import androidx.fragment.app.testing.launchFragmentInContainer

@RunWith(AndroidJUnit4::class)
class ActivityFragmentTest {

    @Test
    fun tabs_switchCorrectly() {
        val scenario = launchFragmentInContainer<ActivityFragment>(
            themeResId = R.style.Theme_Musicboxd
        )

        // Lascia eseguire i post {} del fragment
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // 1) Verifica tab iniziale
        scenario.onFragment { fragment ->
            val tabLayout = fragment.requireView().findViewById<TabLayout>(R.id.tabLayout)
            assertEquals(0, tabLayout.selectedTabPosition)

            // 2) Seleziona il tab 1 direttamente (sei già sul main thread qui)
            tabLayout.getTabAt(1)?.select()
        }

        // 3) Sincronizza la coda UI
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // 4) Verifica lo switch
        scenario.onFragment { fragment ->
            val tabLayout = fragment.requireView().findViewById<TabLayout>(R.id.tabLayout)
            assertEquals(1, tabLayout.selectedTabPosition)
        }
    }
}
