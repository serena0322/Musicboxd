package com.example.musicboxd.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicboxd.R
import android.widget.TextView
import com.example.musicboxd.local.Review
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val reviews: List<Review>,
    private val onDeleteClick: (Review) -> Unit // nuovo parametro
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle = view.findViewById<TextView>(R.id.songTitle)
        val albumCover = view.findViewById<ImageView>(R.id.albumCover)
        val reviewText = view.findViewById<TextView>(R.id.reviewText)
        val rating = view.findViewById<TextView>(R.id.rating)
        val artistName = view.findViewById<TextView>(R.id.artistName)
        val timestamp = view.findViewById<TextView>(R.id.timestamp)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.songTitle.text = review.songTitle
        holder.artistName.text = review.artistName

        // Imposta il testo dell'EditText
        holder.reviewText.setText(review.reviewText)

        holder.itemView.setOnLongClickListener {
            onDeleteClick(review)
            true
        }

        //Per adattare il layout se manca la recensione scritta, gli altri valori salgono più in alto
        val text = review.reviewText.trim()

        if (text.isEmpty()) {
            holder.reviewText.visibility = View.GONE
        } else {
            holder.reviewText.visibility = View.VISIBLE
            holder.reviewText.text = text
        }

        val ratingFormatted = if ((review.rating % 1).toFloat() == 0f) {
            review.rating.toInt().toString()
        } else {
            review.rating.toString()
        }
        holder.rating.text = "$ratingFormatted/5"


        val formattedDate = review.timestamp?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Data non disponibile"
        holder.timestamp.text = formattedDate

        Glide.with(holder.itemView.context)
            .load(review.albumCoverUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(holder.albumCover)
    }

    override fun getItemCount(): Int = reviews.size
}
