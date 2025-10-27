package com.example.musicboxd.`object`

import com.example.musicboxd.local.ActivityItem
import com.example.musicboxd.local.PlaylistItem
import com.example.musicboxd.local.Review
import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class UserRepositoryTest {

    @Test
    fun `mapReview mappa correttamente i campi validi`() {
        val data = mapOf(
            "title" to "Fix You",
            "artist" to "Coldplay",
            "timestamp" to Timestamp(Date(1735830240000L)),
            "rating" to 4.5,
            "textReview" to "Bellissima",
            "cover" to "https://example.com/c.jpg"
        )
        val r: Review? = mapReview("doc123", data, "user42")
        assertNotNull(r)
        r!!
        assertEquals("doc123", r.documentId)
        assertEquals("Fix You", r.songTitle)
        assertEquals("Coldplay", r.artistName)
        assertEquals(4.5, r.rating, 0.0)
        assertEquals("Bellissima", r.reviewText)
        assertEquals("user42", r.sourceUserId)
        assertEquals("https://example.com/c.jpg", r.albumCoverUrl)
    }

    @Test
    fun `mapReview ritorna null se title o artist assenti`() {
        assertNull(mapReview("id", mapOf("artist" to "X"), "u"))
        assertNull(mapReview("id", mapOf("title" to "Y"), "u"))
    }

    @Test
    fun `mapPlaylistItem mappa correttamente`() {
        val ts = Timestamp(Date(1735830240000L))
        val data = mapOf(
            "name" to "Preferite",
            "createdBy" to "user1",
            "timestamp" to ts,
            "tracks" to listOf("t1", "t2")
        )
        val p: PlaylistItem? = mapPlaylistItem("pl1", data)
        assertNotNull(p)
        p!!
        assertEquals("pl1", p.id)
        assertEquals("Preferite", p.name)
        assertEquals("user1", p.createdBy)
        assertEquals(ts, p.timestamp)
        assertEquals(listOf("t1","t2"), p.tracks)
    }

    @Test
    fun `mapActivity ritorna null se campi essenziali mancanti`() {
        val ts = Timestamp(Date())
        assertNull(mapActivity(null, ts))
        assertNull(mapActivity("azione", null))
    }

    @Test
    fun `ordinamento per timestamp desc funziona`() {
        val t1 = Timestamp(Date(1000))
        val t2 = Timestamp(Date(2000))
        val a = listOf(
            ActivityItem("A", t1),
            ActivityItem("B", t2)
        ).sortedByDescending { it.timestamp }
        assertEquals("B", a.first().content)
    }
}
