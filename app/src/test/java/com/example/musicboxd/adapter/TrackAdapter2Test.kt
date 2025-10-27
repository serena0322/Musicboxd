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
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrackAdapter2Test {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    // --- Helper per creare oggetti Track coerenti ---
    private fun fakeTrack(
        id: Long = 1,
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

    // Forza l’elaborazione asincrona del differ
    private fun flushListAdapterQueues() {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushForegroundThreadScheduler()
    }

    // ------------------------------------------------------------
    // TEST
    // ------------------------------------------------------------

    @Test
    fun `submitList aggiorna itemCount`() {
        val adapter = TrackAdapter2 {}
        val tracks = listOf(fakeTrack(1), fakeTrack(2), fakeTrack(3))
        adapter.submitList(tracks)
        flushListAdapterQueues()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia layout e trova le view principali`() {
        val adapter = TrackAdapter2 {}
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.title))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.artist))
        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.cover))
    }

    @Test
    fun `onBindViewHolder popola titolo artista e cover`() {
        val adapter = TrackAdapter2 {}
        val track = fakeTrack(title = "Fix You", artist = "Coldplay", cover = "https://example.com/cover.jpg")
        adapter.submitList(listOf(track))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val titleView = holder.itemView.findViewById<TextView>(R.id.title)
        val artistView = holder.itemView.findViewById<TextView>(R.id.artist)
        val coverView = holder.itemView.findViewById<ImageView>(R.id.cover)

        assertEquals("Fix You", titleView.text.toString())
        assertEquals("Coldplay", artistView.text.toString())
        assertNotNull("ImageView cover non trovata", coverView)
    }

    @Test
    fun `click su item invoca onTrackClick con la canzone corretta`() {
        var clickedTrack: Track? = null
        val adapter = TrackAdapter2 { clickedTrack = it }
        val track = fakeTrack(title = "Clocks", artist = "Coldplay")
        adapter.submitList(listOf(track))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performClick()

        assertNotNull(clickedTrack)
        assertEquals("Clocks", clickedTrack!!.title)
        assertEquals("Coldplay", clickedTrack!!.artist?.name)
    }

    @Test
    fun `DiffCallback confronta titolo e contenuti correttamente`() {
        val diff = TrackAdapter2.DiffCallback()

        val t1 = fakeTrack(1, title = "Yellow", artist = "Coldplay")
        val t2 = fakeTrack(2, title = "Yellow", artist = "Coldplay") // stesso titolo
        val t3 = fakeTrack(3, title = "Viva la Vida", artist = "Coldplay")

        assertTrue("Titoli uguali → sono lo stesso item", diff.areItemsTheSame(t1, t2))
        assertFalse("Oggetti diversi → contenuti diversi", diff.areContentsTheSame(t1, t2))
        assertFalse("Titoli diversi → non lo stesso item", diff.areItemsTheSame(t1, t3))
    }
}
