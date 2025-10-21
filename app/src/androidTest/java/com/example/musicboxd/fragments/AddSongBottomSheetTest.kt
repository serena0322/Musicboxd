package com.example.musicboxd.fragments

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.musicboxd.R
import com.example.musicboxd.adapter.TrackAdapter2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test essenziale per AddSongBottomSheet:
 * - mostra il bottom sheet in una Activity host minimale;
 * - verifica presenza e tipo dei componenti principali.
 */

@RunWith(AndroidJUnit4::class)
class AddSongBottomSheetTest {

    @Test
    fun shows_core_components() {
        // Avvia un'Activity host minimale
        val scenario = ActivityScenario.launch(BottomSheetHostActivity::class.java)

        scenario.onActivity { activity ->
            // Mostra il BottomSheet
            val sheet = AddSongBottomSheet()
            sheet.show(activity.supportFragmentManager, "AddSongBottomSheet")
        }

        // Sincronizza la coda UI
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("AddSongBottomSheet") as AddSongBottomSheet
            val root = fragment.requireView()

            // 1) RecyclerView presente e con LayoutManager lineare
            val rv = root.findViewById<RecyclerView>(R.id.addScrollView)
            assertNotNull("RecyclerView non trovata", rv)
            assertTrue("LayoutManager non è LinearLayoutManager", rv.layoutManager is LinearLayoutManager)

            // 2) Adapter è TrackAdapter2
            assertTrue("Adapter non è TrackAdapter2", rv.adapter is TrackAdapter2)

            // 3) SearchView presente
            val searchView = root.findViewById<SearchView>(R.id.searchView2)
            assertNotNull("SearchView non trovata", searchView)

            // 4) (facoltativo, ma utile) BottomSheet è in stato espanso
            val sheetView = fragment.dialog?.findViewById<android.view.View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            if (sheetView != null) {
                val behavior = BottomSheetBehavior.from(sheetView)
                assertTrue(
                    "BottomSheet non è in stato espanso",
                    behavior.state == BottomSheetBehavior.STATE_EXPANDED
                            || sheetView.height > activity.resources.displayMetrics.heightPixels * 0.7 // tolleranza
                )
            }
        }

        scenario.close()
    }
}

