package com.example.musicboxd

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        val title = intent.getStringExtra("title")
        val artist = intent.getStringExtra("artist")
        val coverUrl = intent.getStringExtra("cover")

        findViewById<TextView>(R.id.title).text = title
        findViewById<TextView>(R.id.artist).text = artist
        val coverImageView = findViewById<ImageView>(R.id.cover)
        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person) // immagine di default durante il caricamento
            .error(R.drawable.person)       // immagine se il caricamento fallisce
            .into(coverImageView)

        val heartImage = findViewById<ImageView>(R.id.heartImage)
        val likeText = findViewById<TextView>(R.id.textView6)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val textView = findViewById<TextView>(R.id.textView5)

        heartImage.setOnClickListener {
            val isLiked = !heartImage.isSelected
            heartImage.isSelected = isLiked
            likeText.text = if (isLiked) "Liked" else "Like"
        }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Log.d("ReviewActivity", "Rating changed: $rating")
            textView.text = if (rating > 0) "Rated" else "Rate"
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
