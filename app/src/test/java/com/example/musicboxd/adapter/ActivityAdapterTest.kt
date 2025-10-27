package com.example.musicboxd.adapter

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.musicboxd.R
import com.example.musicboxd.local.ActivityItem
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ActivityAdapterTest {
    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    private var defaultLocale: Locale? = null
    private var defaultTz: TimeZone? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)

        defaultLocale = Locale.getDefault()
        defaultTz = TimeZone.getDefault()

        Locale.setDefault(Locale.ITALY)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        defaultLocale?.let { Locale.setDefault(it) }
        defaultTz?.let { TimeZone.setDefault(it) }
    }

    @Test
    fun `getItemCount riflette la dimensione iniziale`() {
        val items = listOf(
            ActivityItem(
                content = "A",
                timestamp = com.google.firebase.Timestamp(Date(1735830240000))
            )
        )
        val adapter = ActivityAdapter(0, items)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia layout e trova le view`() {
        val adapter = ActivityAdapter(0, emptyList())
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.activityText))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.date))
    }

    @Test
    fun `onBindViewHolder assegna testo e data formattata`() {
        val ts = com.google.firebase.Timestamp(Date(1735830240000))
        val items = listOf(ActivityItem(content = "Messaggio", timestamp = ts))
        val adapter = ActivityAdapter(0, items)
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Messaggio", holder.content.text.toString())
        assertEquals("02/01/2025 15:04", holder.date.text.toString())
    }

    @Test
    fun `onBindViewHolder mostra fallback se timestamp nullo`() {
        val items = listOf(ActivityItem(content = "Senza data", timestamp = null))
        val adapter = ActivityAdapter(0, items)
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Senza data", holder.content.text.toString())
        assertEquals("Data non disponibile", holder.date.text.toString())
    }

    @Test
    fun `updateData sostituisce la lista e notifica esattamente una volta`() {
        val adapter = ActivityAdapter(0, emptyList())

        var onChangedCount = 0
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() { onChangedCount++ }
        }
        adapter.registerAdapterDataObserver(observer)

        val newItems = List(3) {
            ActivityItem("Item $it", com.google.firebase.Timestamp(Date(1735840000000L + it)))
        }
        adapter.updateData(newItems)

        assertEquals(3, adapter.itemCount)
        assertEquals(1, onChangedCount)
    }

    @Test
    fun `updateTabIndex aggiorna lo stato interno`() {
        val adapter = ActivityAdapter(0, emptyList())
        adapter.updateTabIndex(2)

        val field = ActivityAdapter::class.java.getDeclaredField("tabIndex")
        field.isAccessible = true
        val value = field.getInt(adapter)
        assertEquals(2, value)
    }
}
