package com.example.musicboxd.adapter

import android.content.Context
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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
@LooperMode(LooperMode.Mode.LEGACY) // evita l'eccezione "PAUSED mode" con ListAdapter/AsyncListDiffer
class TrackAdapter3Test {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    // --- Helper dati coerenti con network.Track ---
    private fun fakeTrack(
        id: Long = 1L,
        title: String = "Song $id",
        artist: String = "Artist $id",
        cover: String? = "https://example.com/$id.jpg"
    ): Track {
        return Track(
            id = id,
            title = title,
            artist = Artist(name = artist),
            album = Album(
                id = id,
                title = "Album $id",
                genres = GenreResponse(emptyList()),
                cover = cover,
                releaseDate = "2025-01-01"
            ),
            duration = 200,
            preview = null
        )
    }

    private fun flushListAdapterQueues() {
        shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushForegroundThreadScheduler()
        Robolectric.flushBackgroundThreadScheduler()
    }

    @Test
    fun `submitList aggiorna itemCount`() {
        val adapter = TrackAdapter3(onItemClick = {}, onLongClick = {})
        val data = listOf(fakeTrack(1), fakeTrack(2), fakeTrack(3))
        adapter.submitList(data)
        flushListAdapterQueues()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_track2 e trova le view`() {
        val adapter = TrackAdapter3({}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.title))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.artist))
        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `onBindViewHolder popola titolo artista e carica cover`() {
        val adapter = TrackAdapter3({}, {})
        adapter.submitList(listOf(fakeTrack(title = "Fix You", artist = "Coldplay", cover = "https://example.com/cover.jpg")))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        assertEquals("Fix You", holder.itemView.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("Coldplay", holder.itemView.findViewById<TextView>(R.id.artist).text.toString())
        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover)) // no crash con Glide
    }

    @Test
    fun `onBindViewHolder gestisce cover nulla senza crash`() {
        val adapter = TrackAdapter3({}, {})
        adapter.submitList(listOf(fakeTrack(cover = null)))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `click su item invoca onItemClick con il Track corretto`() {
        var clicked: Track? = null
        val adapter = TrackAdapter3(onItemClick = { clicked = it }, onLongClick = {})
        val t = fakeTrack(title = "Clocks", artist = "Coldplay")
        adapter.submitList(listOf(t))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()

        assertNotNull(clicked)
        assertEquals("Clocks", clicked!!.title)
        assertEquals("Coldplay", clicked!!.artist?.name)
    }

    @Test
    fun `long-click su item invoca onLongClick con il Track corretto`() {
        var longClicked: Track? = null
        val adapter = TrackAdapter3(onItemClick = {}, onLongClick = { longClicked = it })
        val t = fakeTrack(title = "Viva la Vida", artist = "Coldplay")
        adapter.submitList(listOf(t))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performLongClick()

        assertNotNull(longClicked)
        assertEquals("Viva la Vida", longClicked!!.title)
    }

    @Test
    fun `DiffCallback confronta titolo+artista e contenuti`() {
        val diff = TrackAdapter3.DiffCallback()

        val a1 = fakeTrack(1, title = "Yellow", artist = "Coldplay", cover = "https://a.jpg")
        val a2 = fakeTrack(2, title = "Yellow", artist = "Coldplay", cover = "https://b.jpg") // stesso item (title+artist), contenuti diversi
        val b  = fakeTrack(3, title = "Speed of Sound", artist = "Coldplay")

        assertTrue(diff.areItemsTheSame(a1, a2))     // stesso item
        assertFalse(diff.areContentsTheSame(a1, a2)) // contenuto diverso
        assertFalse(diff.areItemsTheSame(a1, b))     // item diverso
    }
}
