package com.example.musicboxd.adapter

import android.content.Context
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@LooperMode(LooperMode.Mode.LEGACY)
class TrackAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    // --- Helpers per creare dati coerenti con le tue data class ---
    private fun fakeTrack(i: Int = 0, title: String = "Song $i", artist: String = "Artist $i", cover: String? = "https://example.com/$i.jpg"): Track {
        return Track(
            id = i.toLong(),
            title = title,
            artist = Artist(name = artist),
            album = Album(
                id = i.toLong(),
                title = "Album $i",
                genres = GenreResponse(emptyList()),
                cover = cover,
                releaseDate = "2025-01-${(i % 28 + 1).toString().padStart(2, '0')}"
            ),
            duration = 180,
            preview = "https://example.com/$i.mp3"
        )
    }

    // Per comodità: forza l’elaborazione asincrona del ListAdapter
    private fun flushListAdapterQueues() {
        shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushForegroundThreadScheduler()
        Robolectric.flushBackgroundThreadScheduler()
    }

    @Test
    fun `submitList aggiorna itemCount`() {
        val adapter = TrackAdapter(onItemClick = {}, onLongClick = {})
        adapter.submitList(listOf(fakeTrack(0), fakeTrack(1), fakeTrack(2)))
        flushListAdapterQueues()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_track e trova le view`() {
        val adapter = TrackAdapter({}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.title))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.artist))
        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `onBindViewHolder imposta titolo e artista e carica cover`() {
        val adapter = TrackAdapter({}, {})
        adapter.submitList(listOf(fakeTrack(0, title = "Fix You", artist = "Coldplay", cover = "https://example.com/cover.jpg")))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        assertEquals("Fix You", holder.itemView.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("Coldplay", holder.itemView.findViewById<TextView>(R.id.artist).text.toString())
        // Verifica "no crash" su Glide: la view esiste
        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `onBindViewHolder gestisce cover nulla senza crash`() {
        val adapter = TrackAdapter({}, {})
        adapter.submitList(listOf(fakeTrack(0, cover = null)))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        // Non deve lanciare eccezioni
        adapter.onBindViewHolder(holder, 0)

        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `click su item invoca onItemClick col Track corretto`() {
        var clicked: Track? = null
        val adapter = TrackAdapter(onItemClick = { clicked = it }, onLongClick = {})
        val t = fakeTrack(7, title = "Yellow", artist = "Coldplay")
        adapter.submitList(listOf(t))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performClick()

        assertNotNull(clicked)
        assertEquals("Yellow", clicked!!.title)
        assertEquals("Coldplay", clicked!!.artist?.name)
    }

    @Test
    fun `long-click su item invoca onLongClick col Track corretto`() {
        var longClicked: Track? = null
        val adapter = TrackAdapter(onItemClick = {}, onLongClick = { longClicked = it })
        val t = fakeTrack(3, title = "Viva la Vida", artist = "Coldplay")
        adapter.submitList(listOf(t))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performLongClick()

        assertNotNull(longClicked)
        assertEquals("Viva la Vida", longClicked!!.title)
    }

    @Test
    fun `DiffCallback areItemsTheSame usa titolo+artista e areContentsTheSame confronta l'intero oggetto`() {
        val diff = TrackAdapter.DiffCallback()

        val a1 = fakeTrack(1, title = "Clocks", artist = "Coldplay", cover = "https://a.jpg")
        val a2 = fakeTrack(2, title = "Clocks", artist = "Coldplay", cover = "https://b.jpg") // stesso titolo+artista, contenuto diverso
        val b  = fakeTrack(3, title = "Speed of Sound", artist = "Coldplay")

        assertTrue(diff.areItemsTheSame(a1, a2))   // titolo+artista uguali
        assertFalse(diff.areContentsTheSame(a1, a2)) // oggetti diversi -> contenuti diversi

        assertFalse(diff.areItemsTheSame(a1, b))   // titolo diversi
        assertFalse(diff.areContentsTheSame(a1, b))
    }
}
