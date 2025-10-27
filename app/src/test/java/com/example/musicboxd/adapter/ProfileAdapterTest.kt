package com.example.musicboxd.adapter

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    @Test
    fun `getItemCount restituisce la dimensione iniziale`() {
        val adapter = ProfileAdapter(listOf("user1", "user2", "user3"))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_user e trova TextView username`() {
        val adapter = ProfileAdapter(emptyList())
        val holder = adapter.onCreateViewHolder(parent, 0)

        val textView = holder.itemView.findViewById<TextView>(R.id.username)
        assertNotNull(textView)
    }

    @Test
    fun `onBindViewHolder assegna correttamente il nome utente`() {
        val adapter = ProfileAdapter(listOf("Serena"))
        val holder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(holder, 0)

        val text = holder.itemView.findViewById<TextView>(R.id.username).text.toString()
        assertEquals("Serena", text)
    }

    @Test
    fun `updateData sostituisce la lista e notifica una sola volta`() {
        val adapter = ProfileAdapter(listOf("OldUser"))

        var notifyCount = 0
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() { notifyCount++ }
        }
        adapter.registerAdapterDataObserver(observer)

        adapter.updateData(listOf("NewUser1", "NewUser2"))

        assertEquals(2, adapter.itemCount)
        assertEquals(1, notifyCount)
    }

    @Test
    fun `lista vuota non causa crash e itemCount è zero`() {
        val adapter = ProfileAdapter(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `onBindViewHolder con piu utenti mostra testo corretto per ciascuno`() {
        val adapter = ProfileAdapter(listOf("Alice", "Bob", "Charlie"))

        for (i in 0 until adapter.itemCount) {
            val holder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(holder, i)
            val displayed = holder.itemView.findViewById<TextView>(R.id.username).text.toString()
            assertEquals(adapterPositionToExpected(i), displayed)
        }
    }

    private fun adapterPositionToExpected(position: Int): String {
        return when (position) {
            0 -> "Alice"
            1 -> "Bob"
            2 -> "Charlie"
            else -> ""
        }
    }
}
