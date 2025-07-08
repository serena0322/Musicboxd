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
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText


class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        val title = intent.getStringExtra("title") ?: "una canzone"
        val artist = intent.getStringExtra("artist") ?: "un artista"
        val coverUrl = intent.getStringExtra("cover") ?: ""

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
        date.text = timestamp.toDate().toString()

        titleText.text = title
        artistText.text = artist

        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(coverImage)

        saveButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val uid = currentUser.uid
            val reviewText = textReviewInput.text?.toString()?.trim() ?: ""
            val rating = ratingBar.rating.toDouble()
            val timestamp = Timestamp.now()

            val db = FirebaseFirestore.getInstance()

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

            // Salvataggio in parallelo ma asincrono
            db.collection("User").document(uid).collection("Reviews").add(reviewData)
            db.collection("User").document(uid).collection("Activity").add(activityData)
            db.collection("User").document(uid).collection("ActivityForOthers").add(publicActivity)

            finish()
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
