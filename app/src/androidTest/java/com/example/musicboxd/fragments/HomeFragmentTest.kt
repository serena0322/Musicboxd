package com.example.musicboxd.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicboxd.R
import com.example.musicboxd.adapter.HomeAdapter
import com.example.musicboxd.adapter.ReviewAdapter
import com.google.android.material.tabs.TabLayout
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals


@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentTest {

    @Test
    fun fragmentInflates_andCoreViewsVisible() {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            // view principali
            assertTrue(root.findViewById<TabLayout>(R.id.tabLayout) != null)
            assertTrue(root.findViewById<RecyclerView>(R.id.homeRecyclerView) != null)

            // layout manager corretto
            val rv = root.findViewById<RecyclerView>(R.id.homeRecyclerView)
            assertTrue(rv.layoutManager is LinearLayoutManager)
        }
    }

    @Test
    fun tabLayout_hasTwoTabs_andInitialIsFirst() {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val tab = fragment.requireView().findViewById<TabLayout>(R.id.tabLayout)
            assertEquals("TabLayout dovrebbe avere 2 tab", 2, tab.tabCount)
            assertEquals("Il tab iniziale dovrebbe essere 0",0, tab.selectedTabPosition, )
        }
    }

    @Test
    fun switchingTabs_changesRecyclerViewAdapter() {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            val rv = root.findViewById<RecyclerView>(R.id.homeRecyclerView)

            // Al primo tab ci aspettiamo HomeAdapter (adegua se la tua logica è diversa)
            assertTrue("Adapter iniziale atteso: HomeAdapter", rv.adapter is HomeAdapter)

            // Seleziona il secondo tab
            tab.getTabAt(1)?.select()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.homeRecyclerView)
            assertTrue("Dopo lo switch l'adapter deve essere ReviewAdapter",rv.adapter is ReviewAdapter, )
        }

        // Torna al primo tab e ricontrolla
        scenario.onFragment { fragment ->
            val root = fragment.requireView()
            val tab = root.findViewById<TabLayout>(R.id.tabLayout)
            tab.getTabAt(0)?.select()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val rv = fragment.requireView().findViewById<RecyclerView>(R.id.homeRecyclerView)
            assertTrue("Tornando al tab 0 l'adapter deve essere di nuovo HomeAdapter", rv.adapter is HomeAdapter, )
        }
    }
}
