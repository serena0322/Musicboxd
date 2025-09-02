package com.example.musicboxd.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicboxd.R
import com.example.musicboxd.local.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val reviews: List<Review>,
    private val onDeleteClick: (Review) -> Unit,
    private val showAuthor: Boolean = false                    // <-- nuovo (default: non mostra)
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Mappa uid -> username per mostrare l’autore quando serve
    private var usernames: Map<String, String> = emptyMap()
    fun updateUsernames(map: Map<String, String>) {
        usernames = map
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumCover: ImageView = view.findViewById(R.id.albumCover)
        val username: TextView? = view.findViewById(R.id.username)           // <-- opzionale
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val artistName: TextView = view.findViewById(R.id.artistName)
        val reviewText: TextView = view.findViewById(R.id.reviewText)
        val rating: TextView = view.findViewById(R.id.rating)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // --- Autore (solo se richiesto e se la view esiste nel layout) ---
        holder.username?.let { tv ->
            if (showAuthor) {
                val uid = review.sourceUserId
                val shown = usernames[uid]
                    ?: uid.takeIf { it.isNotBlank() }?.let { "@${it.take(6)}…" }
                    ?: "@sconosciuto"
                tv.text = shown
                tv.visibility = View.VISIBLE
            } else {
                tv.visibility = View.GONE
            }
        }

        // Titolo e artista
        holder.songTitle.text = review.songTitle
        holder.artistName.text = review.artistName

        // Testo recensione (adatta il layout se vuoto)
        val text = review.reviewText.orEmpty().trim()
        if (text.isEmpty()) {
            holder.reviewText.visibility = View.GONE
        } else {
            holder.reviewText.visibility = View.VISIBLE
            holder.reviewText.text = text
        }

        // Rating (senza .0)
        val ratingFormatted = if ((review.rating % 1).toFloat() == 0f) {
            review.rating.toInt().toString()
        } else {
            review.rating.toString()
        }
        holder.rating.text = "$ratingFormatted/5"

        // Timestamp
        val formattedDate = review.timestamp?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Data non disponibile"
        holder.timestamp.text = formattedDate

        // Cover
        Glide.with(holder.itemView.context)
            .load(review.albumCoverUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(holder.albumCover)

        // Long-press per eliminazione
        holder.itemView.setOnLongClickListener {
            onDeleteClick(review)
            true
        }
    }

    override fun getItemCount(): Int = reviews.size
}
