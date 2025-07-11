package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.example.musicboxd.network.Track
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import org.chromium.base.Log
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewActivity : AppCompatActivity() {

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        val track = intent.getParcelableExtra<Track>("track")

        val title = track?.title ?: "una canzone"
        val artist = track?.artist?.name ?: "un artista"
        val coverUrl = track?.album?.cover ?: ""

        val coverImage = findViewById<ImageView>(R.id.coverImage)
        val titleText = findViewById<TextView>(R.id.title)
        val artistText = findViewById<TextView>(R.id.artist)
        val heartImage = findViewById<ImageView>(R.id.heartImage)
        val likeText = findViewById<TextView>(R.id.like)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val rate = findViewById<TextView>(R.id.rate)
        val saveButton = findViewById<TextView>(R.id.save)
        val textReviewInput = findViewById<TextInputEditText>(R.id.textReview)
        val date = findViewById<TextView>(R.id.textView4)
        val timestamp = Timestamp.now()
        val formatter = SimpleDateFormat("dd MMMM, HH:mm", Locale("it", "IT"))
        val formattedDate = formatter.format(timestamp.toDate())
        date.text = formattedDate

        titleText.text = title
        artistText.text = artist

        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(coverImage)

        saveButton.setOnClickListener {
            saveButton.isEnabled = false
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val uid = currentUser.uid
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("User").document(uid)
            val reviewText = textReviewInput.text?.toString()?.trim() ?: ""
            val rating = ratingBar.rating.toDouble()
            val timestamp = Timestamp.now()
            val songId = track?.id?.toString() ?: return@setOnClickListener

            // Controllo recensione esistente
            userDoc.collection("Reviews")
                .whereEqualTo("title", title)
                .whereEqualTo("artist", artist)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Recensione già esistente
                        saveButton.isEnabled = false
                        textReviewInput.isEnabled = false
                        ratingBar.isEnabled = false
                        Toast.makeText(
                            this,
                            "Hai già recensito questa canzone.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }

                    // Altrimenti procedi con il salvataggio
                    val reviewData = mapOf(
                        "action" to "review",
                        "timestamp" to timestamp,
                        "textReview" to reviewText,
                        "title" to title,
                        "artist" to artist,
                        "rating" to rating,
                        "cover" to coverUrl
                    )

                    val activityData = mapOf(
                        "action" to "Hai recensito \"$title\" di $artist",
                        "timestamp" to timestamp
                    )

                    val publicActivity = mapOf(
                        "actionType" to "review",
                        "sourceUserId" to uid,
                        "songTitle" to title,
                        "artistName" to artist,
                        "timestamp" to timestamp
                    )

                    userDoc.collection("Reviews").add(reviewData)
                    userDoc.collection("Activity").add(activityData)
                    userDoc.collection("ActivityForOthers").add(publicActivity)

                    val songRef = db.collection("Songs").document(songId)
                    val ratingKey = String.format(Locale.US, "%.1f", rating)

                    songRef.get().addOnSuccessListener { snapshot ->
                        val setupTask = if (!snapshot.exists()) {
                            val initialHistogram = mapOf(
                                "0.5" to 0, "1.0" to 0, "1.5" to 0, "2.0" to 0, "2.5" to 0,
                                "3.0" to 0, "3.5" to 0, "4.0" to 0, "4.5" to 0, "5.0" to 0
                            )
                            val initialData = mapOf(
                                "totalRatingSum" to 0.0,
                                "totalRatings" to 0,
                                "ratingsHistogram" to initialHistogram
                            )
                            songRef.set(initialData)
                        } else {
                            Tasks.forResult(null)
                        }

                        setupTask.continueWithTask {
                            db.runTransaction { transaction ->
                                val doc = transaction.get(songRef)
                                val currentSum = doc.getDouble("totalRatingSum") ?: 0.0
                                val currentCount = doc.getLong("totalRatings") ?: 0L
                                val ratingKey = String.format(Locale.US, "%.1f", rating)
                                val fieldPath = FieldPath.of("ratingsHistogram", ratingKey)

                                val histogram = doc.get("ratingsHistogram") as? Map<String, Any> ?: emptyMap()
                                val existingValue = histogram[ratingKey] as? Long

                                if (existingValue == null) {
                                    // Prima inizializza il campo se non esiste
                                    transaction.update(songRef, fieldPath, 0L)
                                }

                                // Dopo aggiorna tutto
                                transaction.update(songRef, "totalRatingSum", currentSum + rating)
                                transaction.update(songRef, "totalRatings", currentCount + 1)
                                transaction.update(songRef, fieldPath, FieldValue.increment(1))
                            }

                        }.addOnSuccessListener {
                            if (heartImage.isSelected) {
                                userDoc.update("likes", FieldValue.increment(1))
                            }
                            finish()
                        }.addOnFailureListener {
                            saveButton.isEnabled = true
                            Toast.makeText(
                                this,
                                "Errore nel salvataggio della recensione",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Errore durante il controllo recensione esistente", it)
                }
        }

        heartImage.setOnClickListener {
                val isLiked = !heartImage.isSelected
                heartImage.isSelected = isLiked
                likeText.text = if (isLiked) "Liked" else "Like"

            }

            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                rate.text = if (rating > 0) "Rated" else "Rate"
            }
    }


        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.clearFocus()
                        hideKeyboard(v)
                    }
                }
            }
            return super.dispatchTouchEvent(ev)
        }

        private fun hideKeyboard(view: View) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

