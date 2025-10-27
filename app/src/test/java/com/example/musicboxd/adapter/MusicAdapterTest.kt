package com.example.musicboxd.adapter

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import com.example.musicboxd.local.MusicItem
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MusicAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    private fun item(t: String, a: String) = MusicItem(title = t, artist = a)

    @Test
    fun `getItemCount riflette la dimensione iniziale`() {
        val adapter = MusicAdapter(listOf(item("A", "X"), item("B", "Y")))
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_home e trova le TextView`() {
        val adapter = MusicAdapter(emptyList())
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.musicTitle))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.musicArtist))
    }

    @Test
    fun `onBindViewHolder assegna titolo e artista`() {
        val items = listOf(item("Song 1", "Artist 1"))
        val adapter = MusicAdapter(items)
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val title = holder.itemView.findViewById<TextView>(R.id.musicTitle).text.toString()
        val artist = holder.itemView.findViewById<TextView>(R.id.musicArtist).text.toString()
        assertEquals("Song 1", title)
        assertEquals("Artist 1", artist)
    }

    @Test
    fun `updateData sostituisce la lista e notifica esattamente una volta`() {
        val adapter = MusicAdapter(emptyList())

        var onChangedCount = 0
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() { onChangedCount++ }
        }
        adapter.registerAdapterDataObserver(observer)

        adapter.updateData(listOf(item("S1", "A1"), item("S2", "A2"), item("S3", "A3")))

        assertEquals(3, adapter.itemCount)
        assertEquals(1, onChangedCount)
    }

    @Test
    fun `lista vuota non causa crash e itemCount è zero`() {
        val adapter = MusicAdapter(emptyList())
        assertEquals(0, adapter.itemCount)
    }
}
