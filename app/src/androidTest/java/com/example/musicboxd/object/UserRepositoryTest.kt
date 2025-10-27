package com.example.musicboxd.`object`

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    @Before
    fun setUp() {
        runBlocking {
            try { FirebaseApp.getInstance() } catch (_: IllegalStateException) {
                FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
            }
            auth = FirebaseAuth.getInstance().apply { useEmulator("10.0.2.2", 9099) }
            db = FirebaseFirestore.getInstance().apply { useEmulator("10.0.2.2", 8080) }

            auth.signInAnonymously().await()
            uid = auth.currentUser!!.uid

            // Profilo base
            db.collection("User").document(uid).set(
                mapOf("username" to "serena", "firstName" to "Serena", "lastName" to "Giuliani")
            ).await()

            // ✅ Usa Timestamp.now() invece di serverTimestamp()
            db.collection("User").document(uid).collection("Reviews").add(
                mapOf(
                    "title" to "Fix You",
                    "artist" to "Coldplay",
                    "rating" to 4.5,
                    "textReview" to "Bellissima",
                    "cover" to "https://example.com/c.jpg",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()

            db.collection("User").document(uid).collection("Playlists").add(
                mapOf(
                    "name" to "Preferite",
                    "createdBy" to uid,
                    "tracks" to listOf("t1","t2"),
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()

            // ✅ Attendi che ci sia almeno 1 doc in entrambe le collezioni
            waitUntilCount("User/$uid/Reviews", minCount = 1)
            waitUntilCount("User/$uid/Playlists", minCount = 1)
        }
    }

    // Helper di attesa "eventuale"
    private suspend fun waitUntilCount(path: String, minCount: Int, timeoutMs: Long = 5_000, stepMs: Long = 150) {
        val start = System.currentTimeMillis()
        val (colPath, subPath) = path.split("/", limit = 2)
        var count = 0
        while (System.currentTimeMillis() - start < timeoutMs) {
            val snap = db.collection(path).get().await()
            count = snap.size()
            if (count >= minCount) return
            delay(stepMs)
        }
        throw AssertionError("Timeout in waitUntilCount('$path'): count=$count < $minCount")
    }

    @After
    fun tearDown() {
        runBlocking {
            try { db.terminate().await() } catch (_: Exception) {}
        }
    }

    @Test
    fun loadMyBasicDataWithReviewsAndPlaylists_returnsCoherentData() = runBlocking {
        val result = UserRepository.loadMyBasicDataWithReviewsAndPlaylists()

        // Asserzioni granulari con messaggi esplicativi
        Assert.assertNotNull("user nullo", result.user)
        Assert.assertTrue("reviews vuote", result.reviews.isNotEmpty())
        Assert.assertTrue("playlists vuote", result.playlists.isNotEmpty())
    }
}
