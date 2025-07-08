package com.example.musicboxd.fragments

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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.`object`.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import org.chromium.base.Log
import java.text.SimpleDateFormat
import java.util.Locale


class ReviewActivity : AppCompatActivity() {

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
            val reviewText = textReviewInput.text?.toString()?.trim() ?: ""
            val rating = ratingBar.rating.toDouble()
            val timestamp = Timestamp.now()
            val songId = track?.id?.toString() ?: return@setOnClickListener

            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("User").document(uid)

            // Dati recensione
            val reviewData = mapOf(
                "action" to "review",
                "timestamp" to timestamp,
                "textReview" to reviewText,
                "title" to title,
                "artist" to artist,
                "rating" to rating,
                "cover" to coverUrl
            )

            // Attività utente
            val activityData = mapOf(
                "action" to "Hai recensito \"$title\" di $artist",
                "timestamp" to timestamp
            )

            // Attività pubblica
            val publicActivity = mapOf(
                "actionType" to "review",
                "sourceUserId" to uid,
                "songTitle" to title,
                "artistName" to artist,
                "timestamp" to timestamp
            )

            // Salvataggio asincrono parallelo
            userDoc.collection("Reviews").add(reviewData)
            userDoc.collection("Activity").add(activityData)
            userDoc.collection("ActivityForOthers").add(publicActivity)

            // Aggiornamento dati aggregati nella canzone
            val songRef = db.collection("Songs").document(songId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(songRef)
                val currentSum = snapshot.getDouble("totalRatingSum") ?: 0.0
                val currentCount = snapshot.getLong("totalRatings") ?: 0L

                transaction.set(
                    songRef,
                    mapOf(
                        "totalRatingSum" to currentSum + rating,
                        "totalRatings" to currentCount + 1
                    ),
                    SetOptions.merge()
                )
            }.addOnSuccessListener {
                Log.d("Firestore", "Dati canzone aggiornati correttamente")
            }.addOnFailureListener {
                saveButton.isEnabled = true
                Toast.makeText(this, "Errore nel salvataggio della recensione", Toast.LENGTH_SHORT).show()
            }

            // Aggiorna contatore dei like se il cuore è selezionato
            if (heartImage.isSelected) {
                userDoc.update("likes", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Log.d("Firestore", "Like incrementato nel profilo utente")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Errore aggiornamento like utente", it)
                    }
            }
            lifecycleScope.launch {
                UserRepository.loadUser()
                finish()
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

