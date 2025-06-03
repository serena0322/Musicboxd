package com.example.musicboxd

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        val title = intent.getStringExtra("title")
        val artist = intent.getStringExtra("artist")
        val cover = intent.getIntExtra("cover", R.drawable.person)

        findViewById<TextView>(R.id.title).text = title
        findViewById<TextView>(R.id.artist).text = artist
        findViewById<ImageView>(R.id.cover).setImageResource(cover)
        }
}