package com.example.musicboxd.adapter

import android.content.Context
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.musicboxd.R
import com.example.musicboxd.local.User
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
@LooperMode(LooperMode.Mode.LEGACY) // evita problemi con AsyncListDiffer in modalità PAUSED
class UserAdapterTest {

    private lateinit var context: Context
    private lateinit var parent: FrameLayout

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    @After
    fun tearDown() { /* no-op */ }

    private fun flushListAdapterQueues() {
        shadowOf(Looper.getMainLooper()).idle()
        Robolectric.flushForegroundThreadScheduler()
        Robolectric.flushBackgroundThreadScheduler()
    }

    // Helper per creare utenti fittizi coerenti
    private fun u(
        id: String,
        username: String,
        first: String = "Name",
        last: String = "Surname"
    ) = User(
        id = id,
        username = username,
        firstName = first,
        lastName = last
    )

    @Test
    fun `submitList aggiorna itemCount`() {
        val adapter = UserAdapter(tabIndex = 0, onUserClick = {})
        val data = listOf(u("1", "alice"), u("2", "bob"), u("3", "carol"))
        adapter.submitList(data)
        flushListAdapterQueues()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder gonfia item_user e trova le view`() {
        val adapter = UserAdapter(tabIndex = 0, onUserClick = {})
        val holder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(holder.itemView.findViewById<TextView>(R.id.username))
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.name))
    }

    @Test
    fun `onBindViewHolder mostra username e nome completo`() {
        val adapter = UserAdapter(tabIndex = 0, onUserClick = {})
        val user = u("1", "serena", "Serena", "Giuliani")
        adapter.submitList(listOf(user))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        val usernameTv = holder.itemView.findViewById<TextView>(R.id.username)
        val nameTv = holder.itemView.findViewById<TextView>(R.id.name)

        assertEquals("serena", usernameTv.text.toString())
        assertEquals("Serena Giuliani", nameTv.text.toString())
    }

    @Test
    fun `click su item invoca onUserClick con l'utente corretto`() {
        var clicked: User? = null
        val adapter = UserAdapter(tabIndex = 0, onUserClick = { clicked = it })
        val user = u("42", "marvin", "Marvin", "Bot")
        adapter.submitList(listOf(user))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.performClick()

        assertNotNull(clicked)
        assertEquals("42", clicked!!.id)
        assertEquals("marvin", clicked!!.username)
    }

    @Test
    fun `long click non crasha e ritorna true senza dipendere dai menu`() {
        // Usiamo tabIndex=1 così non viene inflato alcun menu (evitiamo dipendenze da risorse R.menu)
        val adapter = UserAdapter(tabIndex = 1, onUserClick = {})
        adapter.submitList(listOf(u("1", "alice")))
        flushListAdapterQueues()

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)

        // Il listener ritorna true dal codice dell'adapter
        val handled = holder.itemView.performLongClick()
        assertTrue(handled)
    }

    @Test
    fun `updateTabIndex aggiorna lo stato interno`() {
        val adapter = UserAdapter(tabIndex = 0, onUserClick = {})
        adapter.updateTabIndex(2)

        val field = UserAdapter::class.java.getDeclaredField("tabIndex")
        field.isAccessible = true
        val value = field.getInt(adapter)
        assertEquals(2, value)
    }

    @Test
    fun `UserDiffCallback confronta id e contenuti`() {
        val diff = UserAdapter.UserDiffCallback()

        val a1 = u("1", "alice", "A", "One")
        val a2 = u("1", "alice", "A", "One") // stesso oggetto (== true se data class)
        val b  = u("2", "bob", "B", "Two")

        assertTrue(diff.areItemsTheSame(a1, a2))
        assertTrue(diff.areContentsTheSame(a1, a2))

        assertFalse(diff.areItemsTheSame(a1, b))
        assertFalse(diff.areContentsTheSame(a1, b))
    }
}
