package com.example.musicboxd.adapter

import android.content.Context
import android.widget.FrameLayout
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import com.example.musicboxd.local.PlaylistItem
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlaylistAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    // Helper (adatta se PlaylistItem ha più campi)
    private fun pl(name: String) = PlaylistItem(name = name)

    @Test
    fun `getItemCount restituisce la dimensione iniziale`() {
        val adapter = PlaylistAdapter(mutableListOf(pl("A"), pl("B")), {}, {})
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_playlist e trova playlistName`() {
        val adapter = PlaylistAdapter(mutableListOf(), {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.playlistName))
    }

    @Test
    fun `onBindViewHolder mostra correttamente il nome`() {
        val adapter = PlaylistAdapter(mutableListOf(pl("Rock")), {}, {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val tv = holder.itemView.findViewById<TextView>(R.id.playlistName)
        assertEquals("Rock", tv.text.toString())
    }

    @Test
    fun `click su item invoca onItemClick con la playlist giusta`() {
        var clicked: PlaylistItem? = null
        val playlists = mutableListOf(pl("Chill"), pl("Focus"))
        val adapter = PlaylistAdapter(
            playlists,
            onItemClick = { clicked = it },
            onLongClick = {}
        )

        // Attach a RecyclerView reale per avere bindingAdapterPosition valido
        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        parent.addView(rv)

        // Misura e layout per creare le view
        rv.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        rv.layout(0, 0, 1080, 1920)

        val vh = rv.findViewHolderForAdapterPosition(1)!!  // "Focus"
        vh.itemView.performClick()

        assertNotNull(clicked)
        assertEquals("Focus", clicked!!.name)
    }

    @Test
    fun `long click su item invoca onLongClick con la playlist giusta`() {
        var longClicked: PlaylistItem? = null
        val playlists = mutableListOf(pl("Gym"))
        val adapter = PlaylistAdapter(
            playlists,
            onItemClick = {},
            onLongClick = { longClicked = it }
        )

        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        parent.addView(rv)
        rv.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        rv.layout(0, 0, 1080, 1920)

        val vh = rv.findViewHolderForAdapterPosition(0)!!
        vh.itemView.performLongClick()

        assertNotNull(longClicked)
        assertEquals("Gym", longClicked!!.name)
    }

    @Test
    fun `addPlaylist inserisce in testa, notifica una sola volta e aggiorna itemCount`() {
        val adapter = PlaylistAdapter(mutableListOf(pl("Old1"), pl("Old2")), {}, {})

        var insertedAt: Int? = null
        var onChanged = 0
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                insertedAt = positionStart
            }
            override fun onChanged() { onChanged++ } // non dovrebbe essere chiamato
        }
        adapter.registerAdapterDataObserver(observer)

        val newPl = pl("NewTop")
        adapter.addPlaylist(newPl)

        assertEquals(3, adapter.itemCount)
        assertEquals(0, insertedAt)            // inserita in posizione 0
        assertEquals("NewTop", /* top name */ newPl.name)
        assertEquals("NewTop", /* verify order */ (/* reflect internal */ true).let {
            // Verifica indirettamente che l'ordine sia corretto creando un VH e bindando pos 0
            val holder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(holder, 0)
            holder.itemView.findViewById<TextView>(R.id.playlistName).text.toString()
        })
        assertEquals(0, onChanged)             // nessun notifyDataSetChanged
    }
}
