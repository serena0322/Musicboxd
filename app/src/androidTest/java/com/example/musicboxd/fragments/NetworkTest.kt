package com.example.musicboxd.fragments

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicboxd.R
import com.example.musicboxd.adapter.UserAdapter
import com.google.android.material.tabs.TabLayout
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class NetworkTest {

    @Test
    fun fragmentInflates_coreViewsPresent() {
        val scenario = launchFragmentInContainer<Network>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val root = fragment.requireView()

            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val rv  = root.findViewById<RecyclerView>(R.id.activityRecyclerView)
            val search = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

            assertTrue("TabLayout mancante", tab != null)
            assertTrue("RecyclerView mancante", rv != null)
            assertTrue("SearchView mancante", search != null)

            assertTrue("LayoutManager non è LinearLayoutManager", rv.layoutManager is LinearLayoutManager)
            assertTrue("Adapter non è UserAdapter", rv.adapter is UserAdapter)
        }
    }

    @Test
    fun initialTab_isSearch_andSearchViewVisible() {
        val scenario = launchFragmentInContainer<Network>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val search = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

            // Di default il primo tab (pos 0) è selezionato
            assertEquals("Il tab iniziale dovrebbe essere 0 (Search)", 0, tab.selectedTabPosition)
            assertEquals("La SearchView dovrebbe essere VISIBILE al tab 0",
                android.view.View.VISIBLE, search.visibility)
        }
    }

    @Test
    fun switchingToFollowers_hidesSearchView() {
        val scenario = launchFragmentInContainer<Network>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // 1) Azione UI sul main thread (ok dentro onFragment)
        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            tab.getTabAt(1)?.select() // Followers
        }

        // 2) Sincronizza FUORI dal main thread
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // 3) Asserzioni (puoi farle dentro onFragment perché sono letture)
        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val search = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

            assertEquals(1, tab.selectedTabPosition)
            assertEquals(View.GONE, search.visibility)
        }
    }


    @Test
    fun switchingToFollowing_hidesSearchView_thenBackToSearch_showsSearchView() {
        val scenario = launchFragmentInContainer<Network>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        // 1) Vai a "Following" (tab 2) – azione UI sul main thread
        scenario.onFragment { fragment ->
            val tab = fragment.requireView().findViewById<TabLayout>(R.id.tabLayout)
            tab.getTabAt(2)?.select()
        }
        // 2) Attendi fuori dal main thread
        onView(isRoot()).perform(idleFor(200))

        // 3) Asserzioni (lettura UI) in un nuovo onFragment
        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val search = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

            assertEquals(View.GONE, search.visibility)
            assertEquals(2, tab.selectedTabPosition)
        }

        // 4) Torna a "Search" (tab 0)
        scenario.onFragment { fragment ->
            val tab = fragment.requireView().findViewById<TabLayout>(R.id.tabLayout)
            tab.getTabAt(0)?.select()
        }
        onView(isRoot()).perform(idleFor(200))

        // 5) Asserzioni finali
        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val search = root.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

            assertEquals(View.VISIBLE, search.visibility)
            assertEquals(0, tab.selectedTabPosition)
        }
    }

    // Attesa espresso-friendly per sincronizzare UI thread
    private fun idleFor(millis: Long): ViewAction = object : ViewAction {
        override fun getDescription() = "Wait for $millis milliseconds"
        override fun getConstraints() = isRoot()
        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }

}
