package com.example.musicboxd.adapter

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import com.example.musicboxd.local.Review
import com.google.firebase.Timestamp
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewAdapterTest {

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

    private fun review(
        uid: String = "user_123456",
        title: String = "Song",
        artist: String = "Artist",
        text: String = "Testo recensione",
        rating: Double = 4.0,
        cover: String = "https://example.com/cover.jpg",
        tsMillis: Long? = 1735830240000L // 02/01/2025 15:04 UTC
    ): Review {
        return Review(
            sourceUserId = uid,
            songTitle = title,
            artistName = artist,
            reviewText = text,
            rating = rating,
            albumCoverUrl = cover,
            timestamp = tsMillis?.let { Timestamp(Date(it)) }
        )
    }

    @Test
    fun `getItemCount restituisce la dimensione iniziale`() {
        val adapter = ReviewAdapter(listOf(review(), review()), {}, false)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_review e trova le view principali`() {
        val adapter = ReviewAdapter(emptyList(), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<ImageView>(R.id.albumCover))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.songTitle))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.artistName))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.reviewText))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.rating))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.timestamp))
        // username opzionale
        holder.itemView.findViewById<TextView>(R.id.username)
    }

    @Test
    fun `binding base popola titolo, artista e testo`() {
        val item = review(title = "Fix You", artist = "Coldplay", text = "Bellissima")
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val titleView = holder.itemView.findViewById<TextView>(R.id.songTitle)
        val artistView = holder.itemView.findViewById<TextView>(R.id.artistName)
        val reviewView = holder.itemView.findViewById<TextView>(R.id.reviewText)
        val coverView = holder.itemView.findViewById<ImageView>(R.id.albumCover)

        assertNotNull("TextView titolo non trovata", titleView)
        assertNotNull("TextView artista non trovata", artistView)
        assertNotNull("TextView recensione non trovata", reviewView)
        assertNotNull("ImageView cover non trovata", coverView)

        assertEquals("Fix You", titleView!!.text.toString())
        assertEquals("Coldplay", artistView!!.text.toString())
        assertEquals("Bellissima", reviewView!!.text.toString())
        assertEquals(View.VISIBLE, reviewView.visibility)
    }

    @Test
    fun `reviewText vuoto nasconde la TextView`() {
        val item = review(text = "   ")
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val tv = holder.itemView.findViewById<TextView>(R.id.reviewText)
        assertEquals(View.GONE, tv.visibility)
    }

    @Test
    fun `rating intero mostra numero senza decimali seguiti da slash 5`() {
        val item = review(rating = 4.0)
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val ratingText = holder.itemView.findViewById<TextView>(R.id.rating).text.toString()
        assertEquals("4/5", ratingText)
    }

    @Test
    fun `rating decimale mantiene i decimali`() {
        val item = review(rating = 4.5)
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val ratingText = holder.itemView.findViewById<TextView>(R.id.rating).text.toString()
        assertEquals("4.5/5", ratingText)
    }

    @Test
    fun `timestamp formattato come dd_MM_yyyy HH_mm in UTC`() {
        val item = review(tsMillis = 1735830240000L)
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val ts = holder.itemView.findViewById<TextView>(R.id.timestamp).text.toString()
        assertEquals("02/01/2025 15:04", ts)
    }

    @Test
    fun `timestamp nullo mostra fallback`() {
        val item = review(tsMillis = null)
        val adapter = ReviewAdapter(listOf(item), {}, false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val ts = holder.itemView.findViewById<TextView>(R.id.timestamp).text.toString()
        assertEquals("Data non disponibile", ts)
    }

    @Test
    fun `showAuthor=false nasconde username se presente a layout`() {
        val item = review(uid = "pippo1234")
        val adapter = ReviewAdapter(listOf(item), {}, showAuthor = false)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val userTv = holder.itemView.findViewById<TextView>(R.id.username)
        if (userTv != null) assertEquals(View.GONE, userTv.visibility)
    }

    @Test
    fun `long press chiama onDeleteClick con la review corretta`() {
        var deleted: Review? = null
        val item = review(title = "SongX")
        val adapter = ReviewAdapter(listOf(item), onDeleteClick = { deleted = it }, showAuthor = false)
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performLongClick()

        assertNotNull(deleted)
        assertEquals("SongX", deleted!!.songTitle)
    }
}
