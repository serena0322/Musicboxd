package com.example.musicboxd.adapter

import android.content.Context
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import com.example.musicboxd.network.Album
import com.example.musicboxd.network.Artist
import com.example.musicboxd.network.GenreResponse
import com.example.musicboxd.network.Track
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* nothing */ }

    // ---- Helper: crea una Track di test con valori minimi validi ----
    private fun fakeTrack(i: Int = 0): Track {
        return Track(
            id = i.toLong(),
            title = "Song $i",
            artist = Artist(name = "Artist $i"),
            album = Album(
                id = i.toLong(),
                title = "Album $i",
                genres = GenreResponse(emptyList()),
                cover = "https://example.com/cover$i.jpg",
                releaseDate = "2025-01-0${(i % 9) + 1}"
            ),
            duration = 180,
            preview = "https://example.com/preview$i.mp3"
        )
    }


    private fun section(title: String, size: Int): TrackSection {
        val tracks = List(size) { fakeTrack(it) }
        return TrackSection(title = title, tracks = tracks)
    }

    @Test
    fun `getItemCount restituisce il numero di sezioni`() {
        val adapter = HomeAdapter(
            sections = listOf(section("A", 3), section("B", 2)),
            onTrackClick = {},
            onTrackLongClick = {}
        )
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_section e trova le view`() {
        val adapter = HomeAdapter(emptyList(), {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.tvSectionTitle))
        val rv = holder.itemView.findViewById<RecyclerView>(R.id.rvSectionTracks)
        assertNotNull(rv)
    }

    @Test
    fun `onBindViewHolder imposta titolo sezione e config orizzontale`() {
        val data = listOf(section("Pop", 5))
        val adapter = HomeAdapter(data, {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val title = holder.itemView.findViewById<TextView>(R.id.tvSectionTitle).text.toString()
        assertEquals("Pop", title)

        val rv = holder.itemView.findViewById<RecyclerView>(R.id.rvSectionTracks)
        assertTrue(rv.layoutManager is LinearLayoutManager)
        val lm = rv.layoutManager as LinearLayoutManager
        assertEquals(LinearLayoutManager.HORIZONTAL, lm.orientation)
        assertTrue(rv.adapter is TrackAdapter3)
    }

    @Test
    fun `bind passa le tracce all'adapter interno`() {
        val data = listOf(section("Top", 7))
        val adapter = HomeAdapter(data, {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val rv = holder.itemView.findViewById<RecyclerView>(R.id.rvSectionTracks)

        // Se TrackAdapter3 è ListAdapter + AsyncListDiffer, attendi la coda main:
        shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushForegroundThreadScheduler()

        val innerCount = rv.adapter?.itemCount ?: -1
        assertEquals(7, innerCount)
    }

    @Test
    fun `secondo bind aggiorna titolo e numero elementi`() {
        val s1 = section("Novità", 3)
        val s2 = section("Consigliati", 9)
        val adapter = HomeAdapter(listOf(s1, s2), {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        // Primo bind → sezione 0
        adapter.onBindViewHolder(holder, 0)
        val rv = holder.itemView.findViewById<RecyclerView>(R.id.rvSectionTracks)
        shadowOf(Looper.getMainLooper()).idle()
        var innerCount = rv.adapter?.itemCount ?: -1
        assertEquals(3, innerCount)
        assertEquals("Novità", holder.itemView.findViewById<TextView>(R.id.tvSectionTitle).text)

        // Secondo bind → sezione 1
        adapter.onBindViewHolder(holder, 1)
        shadowOf(Looper.getMainLooper()).idle()
        innerCount = rv.adapter?.itemCount ?: -1
        assertEquals(9, innerCount)
        assertEquals("Consigliati", holder.itemView.findViewById<TextView>(R.id.tvSectionTitle).text)
    }
}
