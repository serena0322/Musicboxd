package com.example.musicboxd.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter2
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test essenziale per AddSongBottomSheet:
 * - lancia il fragment in un container con il tema dell’app;
 * - verifica la presenza dei componenti principali e la configurazione base.
 */
@RunWith(AndroidJUnit4::class)
class AddSongBottomSheetTest {

    @Test
    fun shows_core_components() {
        // Lancia il BottomSheet come semplice Fragment nel container di test
        val scenario = launchFragmentInContainer<AddSongBottomSheet>(
            themeResId = R.style.Theme_Musicboxd
        )

        // Attendi che la UI sia idle
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onFragment { fragment ->
            val root = fragment.requireView()

            // 1) SearchView presente
            val searchView = root.findViewById<SearchView>(R.id.searchView2)
            assertNotNull("SearchView non trovata", searchView)

            // 2) RecyclerView presente
            val rv = root.findViewById<RecyclerView>(R.id.addScrollView)
            assertNotNull("RecyclerView non trovata", rv)

            // 3) LayoutManager lineare
            assertTrue(
                "LayoutManager non è LinearLayoutManager",
                rv.layoutManager is LinearLayoutManager
            )

            // 4) Adapter è TrackAdapter2 (anche con lista vuota)
            assertTrue(
                "Adapter non è TrackAdapter2",
                rv.adapter is TrackAdapter2
            )
        }
    }
}
