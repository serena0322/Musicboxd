package com.example.musicboxd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.musicboxd.classes.Track
import android.media.MediaPlayer
import com.bumptech.glide.Glide

class InformationActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val track = intent.getParcelableExtra<Track>("track")

        val title = track?.title
        val artist = intent.getStringExtra("artist")
        val albumTitle = track?.album?.title
        val duration = track?.duration
        val releaseDate = track?.album?.release_date
        val lyrics = track?.explicit_lyrics
        val preview = track?.preview
        val rank = track?.rank
        val coverUrl = intent.getStringExtra("cover")

        findViewById<TextView>(R.id.textView8).text = "🎧 Titolo: ${title ?: "N/A"}"
        findViewById<TextView>(R.id.textView9).text = "👤 Artista: ${artist ?: "N/A"}"
        findViewById<TextView>(R.id.textView7).text = "Album: ${albumTitle ?: "N/A"}"
        findViewById<TextView>(R.id.textView10).text = "Durata: ${duration?.toString() ?: "N/A"} sec"
        findViewById<TextView>(R.id.textView11).text = "Data: ${releaseDate ?: "N/A"}"
        findViewById<TextView>(R.id.textView14).text = "Explicit: ${lyrics ?: "N/A"}"
        findViewById<TextView>(R.id.textView12).text = "Rank: ${rank?.toString() ?: "N/A"}"
        val coverImageView = findViewById<ImageView>(R.id.infoCover)
        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.person) // immagine di default durante il caricamento
            .error(R.drawable.person)       // immagine se il caricamento fallisce
            .into(coverImageView)

        val playButton = findViewById<Button>(R.id.button3)

        playButton.setOnClickListener {
            if (!isPlaying && preview != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(preview)
                    prepare()
                    start()
                }
                isPlaying = true
                playButton.text = "⏸️ Stop"

                mediaPlayer?.setOnCompletionListener {
                    playButton.text = "▶️ Preview"
                    isPlaying = false
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                playButton.text = "▶️ Preview"
                isPlaying = false
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
